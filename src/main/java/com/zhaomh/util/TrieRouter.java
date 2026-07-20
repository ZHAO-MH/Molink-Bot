/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

package com.zhaomh.util;

import java.util.List;

public class TrieRouter<T> {
    private final SimpleStringTrie<TrieRouter<T>> children = new SimpleStringTrie<>();
    private T data = null;

    public void put(String[] paths, T data) {
        put(paths, data, 0);
    }

    private void put(String[] paths, T data, int index) {
        if (paths.length == index) {
            this.data = data;
            return;
        }
        TrieRouter<T> node = children.get(paths[index]);
        if (node == null) {
            node = new TrieRouter<>();
            children.put(paths[index], node);
        }
        node.put(paths, data, index + 1);
    }

    public void remove(String[] paths) {
        remove(paths, 0);
    }

    private boolean remove(String[] paths, int index) {
        if (paths.length == index) {
            data = null;
            return children.isEmpty();
        }
        TrieRouter<T> node = children.get(paths[index]);
        if (node == null) {
            return data == null && children.isEmpty();
        }
        if (node.remove(paths, index + 1)) {
            children.remove(paths[index]);
        }
        return data == null && children.isEmpty();
    }

    public T get(String[] paths) {
        return get(paths, 0);
    }

    private T get(String[] paths, int index) {
        if (paths.length == index) {
            return data;
        }
        TrieRouter<T> node = children.get(paths[index]);
        if (node == null) {
            return null;
        }
        return node.get(paths, index+1);
    }

    public List<String> getAllKeys() {
        return children.getAllKeys();
    }

    public int size() {
        return children.size();
    }
}
