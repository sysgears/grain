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

import com.sysgears.grain.preview.ConfigChangeListener
import com.sysgears.grain.taglib.Site
import groovy.util.logging.Slf4j

/**
 * Implementation binder that generates proxy which binds implementation to Site property value on the fly.
 * <p>
 * Usage example:
 * <pre>
 * public @Bean @Primary Highlighter createHighlighter() {
 *     new ImplBinder<Highlighter>(Highlighter.class, ctx, 'features.highlight', 
 *         [pygments: PygmentsHighlighter.class, default: FakeHighlighter.class]).proxy
 * }
 * 
 * Somewhere in the other bean:
 * private @Inject Highlighter highlighter
 * ...
 * highlighter.highlight(code, lang) // Will use either PygmentsHighlighter or FakeHighlighter based on
 *                                   // the value of features.highlight in SiteConfig.groovy at the moment 
 * </pre>
 */
@Slf4j
class ImplBinder<T extends ConfigChangeListener> {
    
    /** An instance of generated Proxy implementation */
    private T proxy
    
    /** Target implementation of the proxy */
    private T proxyTarget
    

    /**
     * Creates an instance of implementation binder.
     *  
     * @param ifc an interface that should be implemented by concrete classes
     * @param config site config
     * @param propertyName the property in SiteConfig.groovy which should be monitored
     * @param implMap a map of [property value -> concrete implementation class],
     *                the value of 'default' is used for fallback concrete implementation
     */
    public ImplBinder(Class<T> ifc, Config config, String propertyName,
                      Map<String, Object> implMap) {
        def map = [:]
        
        ifc.methods.each { method ->
            map."$method.name" = { Object[] args ->
                this.proxyTarget?.invokeMethod(method.name, args)
            }            
        }

        map."configChanged" = { Object[] args ->
            def propertyValue = propertyName.split(/\./).inject(config)
                    { parent, property -> parent?."$property" }
            T impl = implMap.find { it.key == propertyValue }?.value as T
            if (impl == null) {
                impl = implMap['default'] as T
            }
            def oldProxy = this.proxyTarget
            this.proxyTarget = impl
            log.info "Using proxy ${proxyTarget.class.name} for ${propertyName}"
            if (Service.class.isAssignableFrom(ifc) && oldProxy != this.proxyTarget) {
                log.info "Switching from ${oldProxy?.class?.name ?: 'none'} to ${proxyTarget.class.name} service for ${propertyName}"
                ((Service)oldProxy)?.stop()
                ((Service)proxy).start()
            }
        }
        
        if (Service.class.isAssignableFrom(ifc)) {
            addShutdownHook { ((Service)proxy).stop() }
        }

        proxy = map.asType(ifc)
    }

    /**
     * Returns proxy which selects the currently active implementation
     * based on the value of Site property at the moment
     * 
     * @return proxy
     */
    public T getProxy() {
        return proxy
    }
}
