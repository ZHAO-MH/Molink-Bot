package com.zhaomh.util;


import java.util.*;

@Deprecated
public class TrieMap<T extends Object> implements Map<String, T> {
    private Node<T> root = new Node<>();
    private int size = 0;
    private Set<T> values = new HashSet<>();
    private Set<String> keys = new HashSet<>();

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size <= 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return key instanceof String && keys.contains(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return values.contains(value);
    }

    @Override
    public T get(Object key) {
        if (key instanceof String str) {
            char[] chars = str.toCharArray();
            Node<T> node = root;
            for (char c : chars) {
                node = node.children.get(c);
                if (node == null) {
                    return null;
                }
            }
            return node.isWord ? node.value: null;
        }
        return null;
    }

    @Override
    public T put(String key, T value) {
        char[] chars = key.toCharArray();
        Node<T> node = root;
        for (char c : chars) {
            if (!node.children.containsKey(c)) {
                node.children.put(c, new Node<>());
            }
            node = node.children.get(c);
        }
        if (node.isWord || node.value != null) {
            T tempValue = node.value;
            values.remove(node.value);
            node.value = value;
            return tempValue;
        }
        node.value = value;
        values.add(value);
        keys.add(key);
        return null;
    }

    @Override
    public T remove(Object key) {
        if (key instanceof String str) {
            char[] chars = str.toCharArray();
            Node<T> node = root;
            Node<T> lastNode = root;
            for (char c : chars) {
                if (!node.children.containsKey(c)) {
                    return null;
                }
                lastNode = node;
                node = node.children.get(c);
            }
            if (node != null && (node.isWord || node.value != null)) {
                node.isWord = false;
                node.value = null;
                T tempValue = node.value;
                values.remove(node.value);
                keys.remove(key);
                if (!hasChildren(node))
                    lastNode.children.remove(chars[chars.length-1]);
                return tempValue;
            }
        }
        return null;
    }

    private boolean hasChildren(Node<T> node) {
        for (Node<T> child : node.children.values()) {
            if (child != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void putAll(Map<? extends String, ? extends T> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        keys.clear();
        values.clear();
        root = new Node<>();
    }

    @Override
    public Set<String> keySet() {
        return Set.copyOf(keys);
    }

    @Override
    public Collection<T> values() {
        return List.copyOf(values);
    }

    /**
     * Trie Map 你还用这个呢:(
     */
    @Override
    public Set<Entry<String, T>> entrySet() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }


    private static class Node<D> {
        Map<Character, Node<D>> children = new HashMap<>();
        D value = null;
        boolean isWord = false;
    }
}
