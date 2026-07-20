/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

package com.zhaomh.util;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleStringTrie<T>{
    private final Node<T> root = new Node<>();

    public T get(String key) {
        Objects.requireNonNull(key, "Key cannot be null.");
        char[] chars = key.toCharArray();
        Node<T> node = root;
        for (char c : chars) {
            node = node.children.get(c);
            if (node == null) {
                return null;
            }
        }
        return node.isWord ? node.value: null;
    }

    public void put(String key, T value) {
        Objects.requireNonNull(key, "Key cannot be null.");
        char[] chars = key.toCharArray();
        Node<T> node = root;
        for (char c : chars) {
            if (!node.children.containsKey(c)) {
                node.children.put(c, new Node<>());
            }
            node = node.children.get(c);
        }
        node.value = value;
        node.isWord = true;
    }

    public T remove(Object key) {
        Objects.requireNonNull(key, "Key cannot be null.");
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
                T tempValue = node.value;
                node.value = null;
                if (node.children.isEmpty())
                    lastNode.children.remove(chars[chars.length-1]);
                return tempValue;
            }
        }
        return null;
    }

    public boolean isEmpty() {
        return root.children.isEmpty();
    }

    public List<String> getAllKeys() {
        return getAllKeysHelper(root, new ArrayList<>(), new ArrayList<>());
    }

    private List<String> getAllKeysHelper(Node<T> node, List<Character> path, List<String> keys) {
        if (node == null) return keys;
        if (node.isWord) keys.add(pathToString(path));
        for (Map.Entry<Character, Node<T>> entry : node.children.entrySet()) {
            path.add(entry.getKey());
            getAllKeysHelper(entry.getValue(), path, keys);
            path.removeLast();
        }
        return keys;
    }

    private String pathToString(List<Character> path) {
        char[] chars = new char[path.size()];
        for (int i = 0; i < path.size(); i++) {
            chars[i] = path.get(i);
        }
        return new String(chars);
    }

    private static class Node<D> {
        Map<Character, Node<D>> children = new ConcurrentHashMap<>();
        D value = null;
        boolean isWord = false;
    }

    public int size() {
        return getAllKeys().size();
    }
}
