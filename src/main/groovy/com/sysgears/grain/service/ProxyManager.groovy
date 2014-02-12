package com.sysgears.grain.service

import groovyx.gpars.agent.Agent

/**
 * Proxy manager responsible for proxy creation, proxy targets switching and
 * providing access to collection of all the proxies in the system.
 */
@javax.inject.Singleton
public class ProxyManager {
    
    /** Proxy manager state agent */
    private Agent<ProxyManagerState> proxyManagerState = new Agent(new ProxyManagerState())
    
    /**
     * Creates proxy for given interface.
     * 
     * @param ifc interface that should be implemented by proxy
     * @param override methods to override map
     * 
     * @return proxy implementing given interface
     */
    public <T> T createProxy(final Class<T> ifc, Map override = [:]) {
        def result = proxyManagerState.sendAndWait { ProxyManagerState it ->
            try {
                def map = [:]

                ifc.methods.each { method ->
                    map."$method.name" = { Object[] args ->
                        proxyManagerState.val.getTarget(ifc)?.invokeMethod(method.name, args)
                    }
                }

                map += override

                def proxy = map.asType(ifc)

                it.setProxy(ifc, proxy)
                it
            } catch (e) {
                new AgentError(cause: e)
            }
        }
        if (result instanceof AgentError)
            throw result.cause
                
        result.getProxy(ifc) as T
    }

    /**
     * Gets proxy manager state snapshot.
     * 
     * @return proxy manager state snapshot. 
     */
    public ProxyManagerState getState() {
        proxyManagerState.val
    }

    /**
     * Sets currently used target for the proxy.
     *
     * @param ifc proxy class
     *
     * @param target currently used target
     */
    public void setTarget(final Class ifc, final Object target) {
        proxyManagerState << { ProxyManagerState it ->
            it.setTarget(ifc, target)
        }
    }
}
