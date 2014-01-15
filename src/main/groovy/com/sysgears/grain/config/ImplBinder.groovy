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

import com.google.inject.Injector
import com.sysgears.grain.preview.ConfigChangeListener
import com.sysgears.grain.service.Service
import com.sysgears.grain.service.ServiceManager
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
    private volatile T proxyTarget
    

    /**
     * Creates an instance of implementation binder.
     *  
     * @param ifc an interface that should be implemented by concrete classes
     * @param propertyName the property in SiteConfig.groovy which should be monitored
     * @param implMap a map of [property value -> concrete implementation class],
     *                the value of 'default' is used for fallback concrete implementation
     * @param injector google Guice injector
     */
    public ImplBinder(final Class<T> ifc, final String propertyName,
                      final Map<String, Object> implMap, final Injector injector) {
        ifc.metaClass.foo = {}
        def config = injector.getInstance(Config.class)

        def map = [:]
        
        ifc.methods.each { method ->
            map."$method.name" = { Object[] args ->
                this.proxyTarget?.invokeMethod(method.name, args)
            }
        }

        map."configChanged" = { Object[] args ->
            def propertyValue = propertyName.split(/\./).inject(config)
                    { parent, property -> parent?."$property" }
            T impl = implMap.find { it.key.toString() == propertyValue.toString() }?.value as T
            if (impl == null) {
                impl = implMap['default'] as T
            }
            def isService = Service.class.isAssignableFrom(ifc) && this.proxyTarget != impl
            if (isService) {
                log.info "Switching from ${proxyTarget?.class?.name ?: 'none'} to ${impl.class.name} service for ${propertyName}"
                ((Service)proxy)?.stop()
                ((Service)proxyTarget)?.stop()
            }
            this.proxyTarget = impl
            if (isService && proxyTarget != null) {
                /* def serviceManager = injector.getInstance(ServiceManager.class)

                implMap.values().each {
                    serviceManager.addServiceDependency(proxy as Service, it as Service)
                } */
                /* def serviceManager = injector.getInstance(ServiceManager.class)

                implMap.values().each {
                    serviceManager.registerServiceDependency(proxy as Service, it as Service)
                } */

                ((Service)proxy)?.start()
                ((Service)proxyTarget)?.start()
            }
            log.info "Using proxy ${proxyTarget.class.name} for ${propertyName}"
        }
        
        proxy = map.asType(ifc)
        proxy.metaClass.originalStart = {
            println "Calling ${this.proxyTarget}.originalStart"
            this.proxyTarget.originalStart()
        }
        proxy.metaClass.originalStop = {
            println "Calling ${this.proxyTarget}.originalStop"
            this.proxyTarget.originalStop()
        }

        if (Service.class.isAssignableFrom(ifc)) {
            def serviceManager = injector.getInstance(ServiceManager.class)

            implMap.values().each {
                serviceManager.addRuntimeDependency(it as Service, proxy as Service)
            }
        }
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
