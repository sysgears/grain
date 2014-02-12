package com.sysgears.grain.service

import com.sysgears.grain.config.Config
import com.sysgears.grain.config.ConfigBinder
import groovy.util.logging.Slf4j

import javax.inject.Inject

/**
 * Created by victor on 2/10/14.
 */
@javax.inject.Singleton
@Slf4j
public class ServiceApp {

    private @Inject ServiceManager serviceManager

    private @Inject GuiceSingletonDiscoverer tracker

    private @Inject UserService userService
    
    private @Inject ProxyManager proxyManager

    private @Inject MutableService mutableService
    
    private @Inject Impl1Service impl1
    
    private @Inject Impl2Service impl2

    private @Inject Config config
    
    private @Inject ConfigBinder binder
    
    public void run() {
        serviceManager.start()
        
        def svc = binder.bind(MutableService, 'mutable', [impl1: impl1, impl2: impl2, default: impl1])

        log.info userService.getMessage()
        config.reload([mutable: 'impl2'])
        svc.configChanged()

        log.info userService.getMessage()
    }
}
