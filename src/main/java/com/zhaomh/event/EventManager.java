/*
 * ============================================================
 * 本文件基于 EventAPIRemastered 项目中的 EventManager 类修改而来。
 *
 * 【原始来源】
 *   项目名称  : EventAPIRemastered
 *   原始作者  : lsiem (基于 DarkMagician6 的原始 EventAPI)
 *   原始仓库  : https://github.com/lsiem/EventAPIRemastered
 *   原始路径  : src/com/darkmagician6/eventapi/EventManager.java
 *   遵循协议  : MIT License (详见项目根目录 LICENSE.txt)
 *
 * 【主要修改内容】（相对于原始版本）
 *   1. 包名由 com.darkmagician6.eventapi 改为 com.zhaomh.event。
 *   2. 类由全静态方法 (static) 改为实例化类，并注入 Accessor 依赖。
 *   3. 新增 call(JsonObject json) 重载方法，支持 OneBot 协议 JSON 解析。
 *   4. 内部静态类 MethodData 重构为 Java Record 类型。
 *   5. 增加了日志支持（Logger）。
 *
 * 【修改者】
 *   ZHAO-MH
 *   修改日期：2026
 * ============================================================
 */
package com.zhaomh.event;

import com.google.gson.JsonObject;
import com.zhaomh.core.Accessor;
import com.zhaomh.core.annotation.EventTarget;
import com.zhaomh.event.impl.BaseEvent;
import com.zhaomh.event.impl.GroupMessageEvent;
import com.zhaomh.event.impl.PrivateMessageEvent;
import com.zhaomh.logger.Logger;
import com.zhaomh.logger.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public final class EventManager {

    private final HashMap<Class<? extends Event>, List<MethodData>> REGISTRY_MAP = new HashMap<>();
    private final Logger log = LoggerFactory.getLogger(EventManager.class);
    private final Accessor accessor;

    public void call(JsonObject json) {
        this.call(new BaseEvent(json, accessor));
        String postType = json.get("post_type").getAsString();
        switch (postType) {
            case "message" -> {
                String messageType = json.get("message_type").getAsString();

                if ("group".equals(messageType)) {
                    this.call(new GroupMessageEvent(json, accessor));

                } else if ("private".equals(messageType)) {
                    this.call(new PrivateMessageEvent(json, accessor));
                }
            }
            case "meta_event" -> {
                String metaType = json.get("meta_event_type").getAsString();

                if ("hreatbeat".equals(metaType)) {
                    break;
                }
            }
            default ->{
                log.debug("未处理的事件类型: {}", postType);
                log.debug("事件内容：{}", json);
            }
        }
    }

    public EventManager(Accessor accessor) {
        this.accessor = accessor;
    }

    /**
     * Registers all the methods marked with the EventTarget annotation in the class of the given Object.
     *
     * @param object
     *         Object that you want to register.
     */
    public void register(Object object) {
        for (final Method method : object.getClass().getDeclaredMethods()) {
            if (!isMethodBad(method)) {
                register(method, object);
            }
        }
    }

    /**
     * Registers the methods marked with the EventTarget annotation and that require
     * the specified Event as the parameter in the class of the given Object.
     *
     * @param object
     *         Object that contains the Method you want to register.
     * @param eventClass
     *         class for the marked method we are looking for.
     */
    public void register(Object object, Class<? extends Event> eventClass) {
        for (final Method method : object.getClass().getDeclaredMethods()) {
            if (!isMethodBad(method, eventClass)) {
                register(method, object);
            }
        }
    }

    /**
     * Unregisters all the methods inside the Object that are marked with the EventTarget annotation.
     *
     * @param object
     *         Object of which you want to unregister all Methods.
     */
    public void unregister(Object object) {
        for (final List<MethodData> dataList : REGISTRY_MAP.values()) {
            for (final MethodData data : dataList) {
                if (data.source().equals(object)) {
                    dataList.remove(data);
                }
            }
        }

        cleanMap(true);
    }

    /**
     * Unregisters all the methods in the given Object that have the specified class as a parameter.
     *
     * @param object
     *         Object that implements the Listener interface.
     * @param eventClass
     *         class for the method to remove.
     */
    public void unregister(Object object, Class<? extends Event> eventClass) {
        if (REGISTRY_MAP.containsKey(eventClass)) {
            for (final MethodData data : REGISTRY_MAP.get(eventClass)) {
                if (data.source().equals(object)) {
                    REGISTRY_MAP.get(eventClass).remove(data);
                }
            }

            cleanMap(true);
        }
    }

    /**
     * Registers a new MethodData to the HashMap.
     * If the HashMap already contains the key of the Method's first argument it will add
     * a new MethodData to key's matching list and sorts it based on Priority. @see com.darkmagician6.eventapi.types.Priority
     * Otherwise it will put a new entry in the HashMap with a the first argument's class
     * and a new CopyOnWriteArrayList containing the new MethodData.
     *
     * @param method
     *         Method to register to the HashMap.
     * @param object
     *         Source object of the method.
     */
    private void register(Method method, Object object) {
        Class<? extends Event> indexClass = (Class<? extends Event>) method.getParameterTypes()[0];
        //New MethodData from the Method we are registering.
        final MethodData data = new MethodData(object, method, method.getAnnotation(EventTarget.class).value());

        //Set's the method to accessible so that we can also invoke it if it's protected or private.
        if (!data.target().isAccessible()) {
            data.target().setAccessible(true);
        }

        if (REGISTRY_MAP.containsKey(indexClass)) {
            if (!REGISTRY_MAP.get(indexClass).contains(data)) {
                REGISTRY_MAP.get(indexClass).add(data);
                sortListValue(indexClass);
            }
        } else {
            REGISTRY_MAP.put(indexClass, new CopyOnWriteArrayList<MethodData>() {
                //Eclipse was bitching about a serialVersionUID.
                private static final long serialVersionUID = 666L; {
                    add(data);
                }
            });
        }
    }

    /**
     * Removes an entry based on the key value in the map.
     *
     * @param indexClass
     *         They index key in the map of which the entry should be removed.
     */
    public void removeEntry(Class<? extends Event> indexClass) {
        Iterator<Map.Entry<Class<? extends Event>, List<MethodData>>> mapIterator = REGISTRY_MAP.entrySet().iterator();

        while (mapIterator.hasNext()) {
            if (mapIterator.next().getKey().equals(indexClass)) {
                mapIterator.remove();
                break;
            }
        }
    }

    /**
     * Cleans up the map entries.
     * Uses an iterator to make sure that the entry is completely removed.
     *
     * @param onlyEmptyEntries
     *         If true only remove the entries with an empty list, otherwise remove all the entries.
     */
    public void cleanMap(boolean onlyEmptyEntries) {
        Iterator<Map.Entry<Class<? extends Event>, List<MethodData>>> mapIterator = REGISTRY_MAP.entrySet().iterator();

        while (mapIterator.hasNext()) {
            if (!onlyEmptyEntries || mapIterator.next().getValue().isEmpty()) {
                mapIterator.remove();
            }
        }
    }

    /**
     * Sorts the List that matches the corresponding Event class based on priority value.
     *
     * @param indexClass
     *         The Event class index in the HashMap of the List to sort.
     */
    private void sortListValue(Class<? extends Event> indexClass) {
        List<MethodData> sortedList = new CopyOnWriteArrayList<>();

        for (final byte priority : Priority.VALUE_ARRAY) {
            for (final MethodData data : REGISTRY_MAP.get(indexClass)) {
                if (data.priority() == priority) {
                    sortedList.add(data);
                }
            }
        }

        //Overwriting the existing entry.
        REGISTRY_MAP.put(indexClass, sortedList);
    }

    /**
     * Checks if the method does not meet the requirements to be used to receive event calls from the Dispatcher.
     * Performed checks: Checks if the parameter length is not 1 and if the EventTarget annotation is not present.
     *
     * @param method
     *         Method to check.
     *
     * @return True if the method should not be used for receiving event calls from the Dispatcher.
     *
     * @see EventTarget
     */
    private boolean isMethodBad(Method method) {
        return method.getParameterTypes().length != 1 || !method.isAnnotationPresent(EventTarget.class);
    }

    /**
     * Checks if the method does not meet the requirements to be used to receive event calls from the Dispatcher.
     * Performed checks: Checks if the parameter class of the method is the same as the event we want to receive.
     *
     * @param method
     *         Method to check.
     * @param eventClass
     *         of the Event we want to find a method for receiving it.
     *
     * @return True if the method should not be used for receiving event calls from the Dispatcher.
     *
     * @see EventTarget
     */
    private boolean isMethodBad(Method method, Class<? extends Event> eventClass) {
        return isMethodBad(method) || !method.getParameterTypes()[0].equals(eventClass);
    }

    /**
     * Call's an event and invokes the right methods that are listening to the event call.
     * First get's the matching list from the registry map based on the class of the event.
     * Then it checks if the list is not null. After that it will check if the event is an instance of
     * EventStoppable and if so it will add an extra check when looping trough the data.
     * If the Event was an instance of EventStoppable it will check every loop if the EventStoppable is stopped, and if
     * it is it will break the loop, thus stopping the call.
     * For every MethodData in the list it will invoke the Data's method with the Event as the argument.
     * After that is all done it will return the Event.
     *
     * @param event
     *         Event to dispatch.
     *
     * @return Event in the state after dispatching it.
     */
    public <T extends Event> T call(final T event) {
        List<MethodData> dataList = REGISTRY_MAP.get(event.getClass());

        if (dataList != null) {
            if (event instanceof EventStoppable) {
                EventStoppable stoppable = (EventStoppable) event;

                for (final MethodData data : dataList) {
                    invoke(data, event);

                    if (stoppable.isStopped()) {
                        break;
                    }
                }
            } else {
                for (final MethodData data : dataList) {
                    invoke(data, event);
                }
            }
        }

        return event;
    }

    /**
     * Invokes a MethodData when an Event call is made.
     *
     * @param data
     *         The data of which the targeted Method should be invoked.
     * @param argument
     *         The called Event which should be used as an argument for the targeted Method.
     *
     */
    private void invoke(MethodData data, Event argument) {
        try {
            data.target().invoke(data.source(), argument);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

        private record MethodData(Object source, Method target, byte priority) {

        /**
         * Sets the values of the data.
         *
         * @param source   The source Object of the data. Used by the VM to
         *                 determine to which object it should send the call to.
         * @param target   The targeted Method to which the Event should be send to.
         * @param priority The priority of this Method. Used by the registry to sort
         *                 the data on.
         */
        private MethodData {
        }

            /**
             * Gets the source Object of the data.
             *
             * @return Source Object of the targeted Method.
             */
            @Override
            public Object source() {
                return source;
            }

            /**
             * Gets the targeted Method.
             *
             * @return The Method that is listening to certain Event calls.
             */
            @Override
            public Method target() {
                return target;
            }

            /**
             * Gets the priority value of the targeted Method.
             *
             * @return The priority value of the targeted Method.
             */
            @Override
            public byte priority() {
                return priority;
            }

        }

}
