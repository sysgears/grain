/*
 * Copyright (c) 2013 SysGears, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sysgears.grain.config

import groovy.util.logging.Slf4j

import javax.inject.Named

/**
 * Site config
 */
@Named
@javax.inject.Singleton
@Slf4j
class Config implements Map {

    /** Config */
    private Map<String, Object> config = [:]

    /**
     * @inheritDoc
     */
    @Override
    public int size() {
        config.size()
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean isEmpty() {
        config.isEmpty()
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean containsKey(Object o) {
        config.containsKey(o)
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean containsValue(Object o) {
        config.containsValue(o)
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object get(Object o) {
        config.get(o)
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object put(Object k, Object v) {
        throw new RuntimeException("Operation forbidden")
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object remove(Object o) {
        throw new RuntimeException("Operation forbidden")
    }

    /**
     * @inheritDoc
     */
    @Override
    public void putAll(Map map) {
        throw new RuntimeException("Operation forbidden")
    }

    /**
     * @inheritDoc
     */
    @Override
    public void clear() {
        throw new RuntimeException("Operation forbidden")
    }

    /**
     * @inheritDoc
     */
    @Override
    public Set keySet() {
        config.keySet()
    }

    /**
     * @inheritDoc
     */
    @Override
    public Collection values() {
        config.values()
    }

    /**
     * @inheritDoc
     */
    @Override
    public Set<Map.Entry> entrySet() {
        config.entrySet()
    }

    /**
     * Reloads this config with new config map
     * 
     * @param newConfig new config map
     */
    void reload(Map newConfig) {
        config.clear()
        config.putAll(newConfig)
    }
}
