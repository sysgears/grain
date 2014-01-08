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
/**
 * Site change broadcaster is responsible for propagating site changes
 * to all the classes that listen to them.
 */
@javax.inject.Singleton
class SiteChangeBroadcaster implements SiteChangeListener {

    /**
     * Site change listeners
     */
    private List<SiteChangeListener> listeners

    /**
     * Creates site change broadcaster.
     *
     * @param listeners site change listener list
     */
    public SiteChangeBroadcaster(SiteChangeListener... listeners) {
        this.listeners = listeners
    }

    /**
     * Propagates site change event to all attached listeners. 
     */
    @Override
    void siteChanged() {
        listeners*.siteChanged()
    }
}