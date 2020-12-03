/*
 * MIT License
 * 
 * Copyright (c) 2020 Hochschule Esslingen
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE. 
 */
package de.hsesslingen.keim.efs.middleware.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores items of type {@link V} in a map by an key of type {@link K}..
 *
 * @author boesch
 * @param <V>
 * @param <K>
 */
public class Cache<K, V> {

    private Duration expiryDuration;
    private final Map<K, Item> map = new ConcurrentHashMap<>();

    public Cache() {
    }

    public Cache(Duration expiryDuration) {
        this.expiryDuration = expiryDuration;
    }

    /**
     * Returns the item expiry duration set for this cache.
     *
     * @return
     */
    protected Duration getExpiryDuration() {
        return expiryDuration;
    }

    /**
     * Sets the item expiry duration for this cache.
     *
     * @param expiryDuration
     */
    protected void setExpiryDuration(Duration expiryDuration) {
        this.expiryDuration = expiryDuration;
    }

    /**
     * Sets a value for the given id.
     *
     * @param key
     * @param value
     */
    public void set(K key, V value) {
        if (map.containsKey(key)) {
            map.get(key).setItem(value);
        } else {
            map.put(key, new Item(value));
        }
    }

    /**
     * Sets a value for the given id.
     *
     * @param key
     * @param value
     */
    public void put(K key, V value) {
        set(key, value);
    }

    /**
     * Removes the value under {@link key} from the cache.
     *
     * @param key
     */
    public void remove(K key) {
        this.map.remove(key);
    }

    /**
     * Removes all key value pairs from the cache.
     */
    public void clear() {
        this.map.clear();
    }

    /**
     * Gets the value of the given id.
     *
     * @param key
     * @return
     */
    public Optional<V> get(K key) {
        if (map.containsKey(key)) {
            return Optional.of(map.get(key).getItem());
        } else {
            return Optional.empty();
        }
    }

    /**
     * Removes expired items from the cache.
     */
    public void cleanUp() {
        if (expiryDuration != null) {
            var now = Instant.now();
            map.values().removeIf(item -> item.getLastUpdated().plus(expiryDuration).isBefore(now));
        }
    }

    /**
     * Wrapper class to store an element in the cache, containing meta
     * information (update timestamp).
     */
    private class Item {

        private V item;
        private Instant lastUpdated;

        public Item(V item) {
            this.setItem(item);
        }

        public V getItem() {
            return item;
        }

        public final void setItem(V item) {
            this.item = item;
            this.lastUpdated = Instant.now();
        }

        public Instant getLastUpdated() {
            return lastUpdated;
        }

    }
}
