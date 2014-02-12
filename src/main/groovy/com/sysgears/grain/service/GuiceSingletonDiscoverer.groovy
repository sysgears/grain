package com.sysgears.grain.service

import com.google.inject.Injector
import com.google.inject.Key
import com.google.inject.Scopes
import groovy.util.logging.Slf4j

import javax.inject.Inject
import javax.inject.Provider

/**
 * Discovers Guice singletons and registers them in DependencyTracker.
 */
@javax.inject.Singleton
@Slf4j
public class GuiceSingletonDiscoverer {

    /** Tracks dependencies between objects */
    @Inject private DependencyTracker tracker
    
    /** Guice injector */
    @Inject private Injector injector

    /**
     * Discovers static dependencies between singletons using
     * information from Guice.
     */
    public void discover() {
        def singletons = injector.allBindings.findResults { Key key, com.google.inject.Binding value ->
            if (Scopes.isSingleton(value)) {
                def singleton = injector.getInstance(key)
                singleton.class == null ? null : singleton
            } else {
                null
            }
        } as List<Class>
        
        singletons.each {
            discoverStaticDeps(it)
        }
    }

    /**
     * Discovers all static dependencies for the given singleton object 
     *
     * @param user object to build dependencies for 
     */
    private void discoverStaticDeps(final Object user) {
        log.trace("Traversing ${user.class} to discover static deps")
        user.class.declaredFields.each {
            if (it.isAnnotationPresent(Inject.class) && it.type != Provider) {
                def supplier = injector.getInstance(it.type)
                if (Scopes.isSingleton(injector.getBinding(it.type)) && supplier.class != null) {
                    log.trace("  depends on ${it.type}")
                    tracker.addLink(user, supplier)
                }
            }
        }
    }
}
