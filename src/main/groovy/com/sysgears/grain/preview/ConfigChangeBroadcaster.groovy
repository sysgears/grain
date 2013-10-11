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

package com.sysgears.grain.preview

import javax.inject.Named

/**
 * Config change broadcaster is responsible for propagating site config changes
 * to all the classes that listen to them.
 */
@Named
@javax.inject.Singleton
class ConfigChangeBroadcaster implements ConfigChangeListener {

    /**
     * Config change listeners
     */
    private List<ConfigChangeListener> listeners

    /**
     * Creates config change broadcaster.
     *  
     * @param listeners config change listener list
     */
    public ConfigChangeBroadcaster(ConfigChangeListener... listeners) {
        this.listeners = listeners
    }
    
    /**
     * Propagates site config changes to all the listeners
     */
    @Override
    void configChanged() {
        listeners*.configChanged()
    }
}
