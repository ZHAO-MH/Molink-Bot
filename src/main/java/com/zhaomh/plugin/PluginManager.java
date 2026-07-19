package com.zhaomh.plugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.zhaomh.context.PluginContext;
import com.zhaomh.core.Plugin;
import com.zhaomh.core.ServiceRegistry;
import com.zhaomh.core.annotation.Core;
import com.zhaomh.core.annotation.Require;
import com.zhaomh.logger.Logger;
import com.zhaomh.logger.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PluginManager {
    private static final Logger log = LoggerFactory.getLogger(PluginManager.class);
    private static final Path PLUGIN_STATE_FILE = Paths.get("./data/plugins.json");
    private static final Path PLUGIN_DIR = Paths.get("./plugins");

    private final ServiceRegistry registry;
    // 所有插件实例（key: 类, value: 实例）
    private final Map<Class<?>, Plugin> pluginInstances = new LinkedHashMap<>();
    // 所有插件类名 -> 类（用于查找）
    private final Map<String, Class<?>> allPluginClasses = new HashMap<>();
    // 插件启用状态（name -> true/false）
    private final Map<String, Boolean> pluginStates = new LinkedHashMap<>();
    // 外部插件的 ClassLoader 引用（用于后续卸载/热重载，key为插件name）
    private final Map<String, ClassLoader> externalClassLoaders = new HashMap<>();

    public PluginManager(ServiceRegistry registry) {
        this.registry = registry;
    }

    public void loadPlugins(List<Class<? extends Plugin>> pluginClasses) {
        // 1. 拓扑排序
        List<Class<? extends Plugin>> sorted = topologicalSort(pluginClasses);

        // 2. 初始化所有插件状态为 true（默认启用）
        for (Class<?> clazz : sorted) {
            String name = clazz.getSimpleName();
            pluginStates.put(name, false);
            allPluginClasses.put(name, clazz);
        }

        // 3. 加载持久化状态（覆盖默认值）
        loadPluginStates();

        // 4. 遍历排序结果，全部实例化并执行 onLoad
        for (Class<? extends Plugin> clazz : sorted) {
            String name = clazz.getSimpleName();
            loadAndRegisterPlugin(clazz, name);
        }

        savePluginStates();
        log.info("所有内部插件加载完成，共 {} 个", pluginInstances.size());
    }

    /**
     * 扫描指定目录，加载所有外部插件（.jar 或目录结构）。
     * 每个外部插件拥有独立的 ClassLoader，既能隔离第三方库，又能共享框架核心 API。
     */
    public void loadExternalPlugins() {
        if (!Files.exists(PLUGIN_DIR)) {
            log.warn("外部插件目录不存在: {}", PLUGIN_DIR);
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(PLUGIN_DIR)) {
            for (Path entry : stream) {
                if (entry.toString().endsWith(".jar")) {
                    loadJarPlugin(entry);
                } else if (Files.isDirectory(entry)) {
                    loadDirectoryPlugin(entry);
                }
            }
        } catch (IOException e) {
            log.error("扫描外部插件目录失败", e);
        }

        savePluginStates();
        log.info("所有外部插件加载完成");
    }

    private void loadJarPlugin(Path jarPath) {
        try {
            URL[] urls = { jarPath.toUri().toURL() };
            PluginClassLoader cl = new PluginClassLoader(urls);
            Class<?> pluginClass = scanJarForPluginClass(jarPath, cl);
            if (pluginClass != null) {
                Plugin plugin = (Plugin) pluginClass.getDeclaredConstructor().newInstance();
                // 外部插件以 jar 文件名（不带扩展名）作为唯一标识
                String name = jarPath.getFileName().toString().replace(".jar", "");
                registerExternalPlugin(plugin, name, cl, jarPath);
            } else {
                log.warn("在 {} 中未找到实现 Plugin 的类", jarPath.getFileName());
            }
        } catch (Exception e) {
            log.error("加载 jar 插件失败: {}", jarPath.getFileName(), e);
        }
    }

    private void loadDirectoryPlugin(Path dir) {
        // 查找主 jar（取第一个 .jar 文件，可约定 manifest 指定）
        File[] jars = dir.toFile().listFiles(f -> f.getName().endsWith(".jar"));
        if (jars == null || jars.length == 0) {
            log.warn("插件目录 {} 中没有找到 jar 文件", dir.getFileName());
            return;
        }
        File mainJar = jars[0];
        List<URL> urls = new ArrayList<>();
        try {
            urls.add(mainJar.toURI().toURL());
            File libDir = new File(dir.toFile(), "lib");
            if (libDir.exists() && libDir.isDirectory()) {
                File[] libs = libDir.listFiles(f -> f.getName().endsWith(".jar"));
                if (libs != null) {
                    for (File lib : libs) {
                        urls.add(lib.toURI().toURL());
                    }
                }
            }

            URL[] urlArray = urls.toArray(new URL[0]);
            PluginClassLoader cl = new PluginClassLoader(urlArray);
            Class<?> pluginClass = scanJarForPluginClass(mainJar.toPath(), cl);
            if (pluginClass != null) {
                Plugin plugin = (Plugin) pluginClass.getDeclaredConstructor().newInstance();
                // 以目录名作为插件标识
                String name = dir.getFileName().toString();
                registerExternalPlugin(plugin, name, cl, dir);
            } else {
                log.warn("在目录 {} 的 jar 中未找到 Plugin 实现类", dir.getFileName());
            }
        } catch (Exception e) {
            log.error("加载目录插件失败: {}", dir.getFileName(), e);
        }
    }

    /**
     * 统一注册外部插件实例，纳入现有管理体系
     */
    private void registerExternalPlugin(Plugin plugin, String name,
                                        ClassLoader classLoader, Path source) {
        // 处理名字冲突
        if (allPluginClasses.containsKey(name)) {
            log.warn("插件名冲突: {}，将添加后缀", name);
            name = name + "_" + source.getFileName().toString();
        }

        // 记录 ClassLoader 以备后用
        externalClassLoaders.put(name, classLoader);

        // 注册到内部映射
        Class<?> pluginClass = plugin.getClass();
        allPluginClasses.put(name, pluginClass);
        pluginStates.putIfAbsent(name, false);  // 默认启用
        boolean enabled = pluginStates.getOrDefault(name, false);

        // 核心插件强制启用
        if (pluginClass.isAnnotationPresent(Core.class) && !enabled) {
            log.warn("核心插件 {} 被配置为禁用，已强制启用", name);
            enabled = true;
            pluginStates.put(name, true);
        }

        // 创建上下文并初始化
        PluginContext context = new PluginContext(registry, name);
        if (plugin instanceof BasePlugin base) {
            base.initialize(context);
        }

        // 调用 onLoad
        plugin.onLoad(context);
        log.info("外部插件加载 (onLoad): {}", name);

        // 根据启用状态调用 onEnable/onDisable
        if (plugin instanceof BasePlugin base) {
            if (enabled && !base.isEnabled()) {
                base.setEnabled(true);
            } else if (!enabled && base.isEnabled()) {
                base.setEnabled(false);
            }
        } else {
            if (enabled) {
                plugin.onEnable();
            }
        }

        // 最后放入实例映射
        pluginInstances.put(pluginClass, plugin);
        savePluginStates();
    }


    /**
     * 从插件目录加载一个外部插件（.jar 或目录）
     * @param pluginName 插件名称（对应文件名不含扩展名，或目录名）
     */
    public void loadPluginByName(String pluginName) {
        Path jarPath = PLUGIN_DIR.resolve(pluginName + ".jar");
        if (Files.exists(jarPath)) {
            loadJarPlugin(jarPath);
            savePluginStates();
            return;
        }

        Path dirPath = PLUGIN_DIR.resolve(pluginName);
        if (Files.isDirectory(dirPath)) {
            loadDirectoryPlugin(dirPath);
            savePluginStates();
            return;
        }

        log.error("未找到名为 {} 的外部插件（.jar 或目录）", pluginName);
    }

    public void unloadPluginByName(String name) {
        Class<?> clazz = allPluginClasses.get(name);
        if (clazz == null) {
            log.warn("插件 {} 未加载，无需卸载", name);
            return;
        }

        if (!externalClassLoaders.containsKey(name)) {
            log.error("不允许卸载内部插件: {}", name);
            return;
        }

        if (isPluginRequiredByOthers(name)) {
            log.error("无法卸载插件 {}，仍有其他已加载插件依赖它", name);
            return;
        }

        Plugin plugin = pluginInstances.get(clazz);
        if (plugin != null) {
            try {
                if (plugin instanceof BasePlugin base) {
                    if (base.isEnabled()) {
                        base.setEnabled(false);
                    }
                } else {
                    plugin.onDisable();
                }
                plugin.onUnload();
            } catch (Exception e) {
                log.error("卸载插件 {} 时出错", name, e);
            }
        }

        pluginInstances.remove(clazz);
        allPluginClasses.remove(name);
        pluginStates.remove(name);
        externalClassLoaders.remove(name);

        log.info("插件 {} 已卸载", name);
        savePluginStates();
    }

    /**
     * 重载指定名称的外部插件
     */
    public void reloadPluginByName(String name) {
        if (!externalClassLoaders.containsKey(name)) {
            log.warn("插件 {} 非外部插件或未加载，改为直接加载", name);
            loadPluginByName(name);
            return;
        }
        unloadPluginByName(name);
        loadPluginByName(name);
    }

    /**
     * 检查指定插件是否被其他已加载插件依赖
     */
    private boolean isPluginRequiredByOthers(String name) {
        Class<?> targetClass = allPluginClasses.get(name);
        if (targetClass == null) return false;

        for (Map.Entry<String, Class<?>> entry : allPluginClasses.entrySet()) {
            if (entry.getKey().equals(name)) continue;
            Class<?> clazz = entry.getValue();
            if (clazz.isAnnotationPresent(Require.class)) {
                Require require = clazz.getAnnotation(Require.class);
                for (Class<?> dep : require.value()) {
                    if (dep.isAssignableFrom(targetClass)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void enablePlugin(String name) {
        if (!pluginStates.containsKey(name)) {
            log.warn("未知插件: {}", name);
            return;
        }
        boolean current = pluginStates.get(name);
        if (current) {
            log.info("插件 {} 已经是启用状态", name);
            return;
        }
        pluginStates.put(name, true);
        Plugin plugin = getPluginByName(name);
        if (plugin != null) {
            if (plugin instanceof BasePlugin base) {
                base.setEnabled(true);
            } else {
                plugin.onEnable();
            }
            log.info("插件 {} 已实时启用", name);
        } else {
            log.info("插件 {} 未加载，状态已保存，下次启动生效", name);
        }
        savePluginStates();
    }

    public void disablePlugin(String name) {
        if (!pluginStates.containsKey(name)) {
            log.warn("未知插件: {}", name);
            return;
        }
        Class<?> clazz = allPluginClasses.get(name);
        if (clazz != null && clazz.isAnnotationPresent(Core.class)) {
            log.error("拒绝禁用核心插件: {}", name);
            return;
        }
        boolean current = pluginStates.get(name);
        if (!current) {
            log.info("插件 {} 已经是禁用状态", name);
            return;
        }
        pluginStates.put(name, false);
        Plugin plugin = getPluginByName(name);
        if (plugin != null) {
            if (plugin instanceof BasePlugin base) {
                base.setEnabled(false);
            } else {
                plugin.onDisable();
            }
            log.info("插件 {} 已实时禁用", name);
        } else {
            log.info("插件 {} 未加载，状态已保存", name);
        }
        savePluginStates();
    }

    public boolean isPluginEnabled(String name) {
        return pluginStates.getOrDefault(name, true);
    }

    public List<Plugin> getAllPlugins() {
        return new ArrayList<>(pluginInstances.values());
    }

    public Map<String, Boolean> getAllPluginStates() {
        return new LinkedHashMap<>(pluginStates);
    }

    public Plugin getPluginByName(String name) {
        Class<?> clazz = allPluginClasses.get(name);
        if (clazz != null) {
            return pluginInstances.get(clazz);
        }
        return null;
    }

    public <T extends Plugin> T getPlugin(Class<T> clazz) {
        return (T) pluginInstances.get(clazz);
    }

    public void shutdown() {
        log.info("正在关闭所有插件...");
        for (Plugin plugin : pluginInstances.values()) {
            try {
                if (plugin instanceof BasePlugin base) {
                    if (base.isEnabled()) {
                        base.setEnabled(false);
                    }
                } else {
                    plugin.onDisable();
                }
                plugin.onUnload();
            } catch (Exception e) {
                log.error("关闭插件 {} 时出错", plugin.getClass().getSimpleName(), e);
            }
        }
        savePluginStates();
        // 释放外部 ClassLoader（可选）
        externalClassLoaders.clear();
        log.info("所有插件已关闭");
    }

    private void loadPluginStates() {
        if (!Files.exists(PLUGIN_STATE_FILE)) return;
        try (FileReader reader = new FileReader(PLUGIN_STATE_FILE.toFile())) {
            JsonObject json = new Gson().fromJson(reader, JsonObject.class);
            JsonObject allStates = json.getAsJsonObject("states");
            if (allStates != null) {
                for (String key : allStates.keySet()) {
                    if (pluginStates.containsKey(key)) {
                        pluginStates.put(key, allStates.get(key).getAsBoolean());
                    }
                }
            }
        } catch (IOException e) {
            log.error("加载插件状态失败，使用默认配置", e);
        }
    }

    private void savePluginStates() {
        try {
            Files.createDirectories(PLUGIN_STATE_FILE.getParent());
            JsonObject root = new JsonObject();
            JsonObject states = new JsonObject();
            for (Map.Entry<String, Boolean> entry : pluginStates.entrySet()) {
                states.addProperty(entry.getKey(), entry.getValue());
            }
            root.add("states", states);
            try (FileWriter writer = new FileWriter(PLUGIN_STATE_FILE.toFile())) {
                new GsonBuilder().setPrettyPrinting().create().toJson(root, writer);
            }
        } catch (IOException e) {
            log.error("保存插件状态失败", e);
        }
    }

    // ==================== 拓扑排序 ====================

    private List<Class<? extends Plugin>> topologicalSort(List<Class<? extends Plugin>> pluginClasses) {
        Map<Class<?>, Integer> state = new HashMap<>();
        List<Class<? extends Plugin>> sorted = new ArrayList<>();
        for (Class<?> clazz : pluginClasses) {
            state.put(clazz, 0);
        }
        for (Class<? extends Plugin> clazz : pluginClasses) {
            if (state.get(clazz) == 0) {
                dfs(clazz, pluginClasses, state, sorted);
            }
        }
        return sorted;
    }

    private void dfs(Class<?> current,
                     List<Class<? extends Plugin>> allPlugins,
                     Map<Class<?>, Integer> state,
                     List<Class<? extends Plugin>> sorted) {
        state.put(current, 1);
        List<Class<?>> dependencies = getDependencies(current, allPlugins);
        for (Class<?> dep : dependencies) {
            if (state.get(dep) == null) {
                throw new IllegalStateException("插件 " + current.getSimpleName() +
                        " 依赖了未找到的插件: " + dep.getSimpleName());
            }
            if (state.get(dep) == 1) {
                throw new IllegalStateException("检测到循环依赖: " +
                        current.getSimpleName() + " <-> " + dep.getSimpleName());
            }
            if (state.get(dep) == 0) {
                dfs(dep, allPlugins, state, sorted);
            }
        }
        state.put(current, 2);
        sorted.add((Class<? extends Plugin>) current);
    }

    private List<Class<?>> getDependencies(Class<?> pluginClass,
                                           List<Class<? extends Plugin>> allPlugins) {
        List<Class<?>> deps = new ArrayList<>();
        if (pluginClass.isAnnotationPresent(Require.class)) {
            Require require = pluginClass.getAnnotation(Require.class);
            deps.addAll(Arrays.asList(require.value()));
        }
        List<Class<?>> resolvedDeps = new ArrayList<>();
        for (Class<?> depInterface : deps) {
            for (Class<?> candidate : allPlugins) {
                if (depInterface.isAssignableFrom(candidate) && !candidate.equals(pluginClass)) {
                    resolvedDeps.add(candidate);
                    break;
                }
            }
        }
        for (Class<?> dep : deps) {
            if (Plugin.class.isAssignableFrom(dep) && !resolvedDeps.contains(dep)) {
                resolvedDeps.add(dep);
            }
        }
        return resolvedDeps;
    }

    private void loadAndRegisterPlugin(Class<? extends Plugin> clazz, String name) {
        boolean isCore = clazz.isAnnotationPresent(Core.class);
        boolean enabled = pluginStates.getOrDefault(name, true);
        if (isCore && !enabled) {
            log.warn("核心插件 {} 被配置为禁用，已强制启用", name);
            enabled = true;
            pluginStates.put(name, true);
        }
        try {
            Plugin plugin = clazz.getDeclaredConstructor().newInstance();
            pluginInstances.put(clazz, plugin);
            PluginContext context = new PluginContext(registry, name);
            if (plugin instanceof BasePlugin base) {
                base.initialize(context);
            }
            plugin.onLoad(context);
            log.info("插件加载 (onLoad) 完成: {}", name);
            if (plugin instanceof BasePlugin base) {
                if (enabled && !base.isEnabled()) {
                    base.setEnabled(true);
                } else if (!enabled && base.isEnabled()) {
                    base.setEnabled(false);
                }
            } else {
                if (enabled) plugin.onEnable();
            }
        } catch (Exception e) {
            log.error("插件加载失败: {}", name, e);
        }
    }

    /**
     * 扫描 jar 包，寻找实现 Plugin 接口的类。
     * 注意：这里通过传入的 ClassLoader 来加载 Plugin 接口，避免跨 ClassLoader 类型比较。
     */
    private Class<?> scanJarForPluginClass(Path jarPath, ClassLoader cl) {
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            // 1. 通过传入的 ClassLoader 加载 Plugin 接口类，确保类型一致性
            Class<?> pluginInterface;
            try {
                pluginInterface = cl.loadClass(Plugin.class.getName());
            } catch (ClassNotFoundException e) {
                log.error("无法通过插件 ClassLoader 加载 Plugin 接口，请检查框架包是否可见");
                return null;
            }

            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    String className = entry.getName()
                            .replace("/", ".")
                            .replace(".class", "");
                    try {
                        Class<?> cls = cl.loadClass(className);
                        // 用统一加载的接口进行比较
                        if (pluginInterface.isAssignableFrom(cls) && !cls.isInterface()) {
                            return cls;
                        }
                    } catch (NoClassDefFoundError e) {
                        log.debug("跳过类 {}，缺少依赖: {}", className, e.getMessage());
                    } catch (ClassNotFoundException e) {
                        log.debug("跳过类 {}，找不到类定义", className);
                    } catch (Throwable t) {
                        log.warn("加载类 {} 时发生意外错误: {}", className, t.toString());
                    }
                }
            }
        } catch (IOException e) {
            log.error("扫描 jar 文件失败: {}", jarPath, e);
        }
        return null;
    }

    /**
     * 从 .class 文件路径推断全限定类名（假设目录结构与包名一致）
     */
    private String inferClassName(Path classFile) {
        // 简单处理：以 plugins 为基准，例如 plugins/com/example/MyPlugin.class -> com.example.MyPlugin
        // 实际应用中可根据需要调整
        String path = classFile.toString().replace(File.separatorChar, '.');
        if (path.endsWith(".class")) {
            path = path.substring(0, path.length() - 6);
        }
        // 去掉 plugins 前缀（如果有）
        if (path.startsWith("plugins.")) {
            path = path.substring(8);
        }
        return path;
    }

    /**
     * 每个外部插件使用独立的 PluginClassLoader，
     * 父加载器设为加载 Plugin.class 的 ClassLoader（通常是 AppClassLoader），
     * 保证核心 API 共享，同时第三方库自行隔离。
     * 明确排除框架核心包，确保全系统只有一份核心类。
     */
    private static class PluginClassLoader extends URLClassLoader {

        // 框架核心包前缀，这些包里的类必须由父加载器加载
        private static final Set<String> FRAMEWORK_PACKAGES = Set.of(
                "com.zhaomh.core.",
                "com.zhaomh.context.",
                "com.zhaomh.logger.",
                "com.zhaomh.plugin."  // 插件管理器自身所在的包，也应避免被插件覆盖
        );

        public PluginClassLoader(URL[] urls) {
            super(urls, Plugin.class.getClassLoader());
        }

        @Override
        public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            synchronized (getClassLoadingLock(name)) {
                // 1. 检查是否已加载
                Class<?> c = findLoadedClass(name);
                if (c == null) {
                    // 2. 如果是框架包或 Java 核心包，全部交给父加载器
                    if (isFrameworkOrSystemClass(name)) {
                        c = getParent().loadClass(name);
                    } else {
                        // 3. 否则优先自己加载（打破双亲委派，隔离第三方库）
                        try {
                            c = findClass(name);
                        } catch (ClassNotFoundException ignored) {
                            // 自己找不到，再交给父加载器
                            c = getParent().loadClass(name);
                        }
                    }
                }
                if (resolve) {
                    resolveClass(c);
                }
                return c;
            }
        }

        private boolean isFrameworkOrSystemClass(String name) {
            // Java 核心包
            if (name.startsWith("java.") || name.startsWith("javax.")) {
                return true;
            }
            // 框架核心包
            for (String pkg : FRAMEWORK_PACKAGES) {
                if (name.startsWith(pkg)) {
                    return true;
                }
            }
            return false;
        }
    }
}