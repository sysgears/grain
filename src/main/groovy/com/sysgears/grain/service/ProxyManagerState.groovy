package com.sysgears.grain.service

/**
 * Proxy manager state implementation.
 */
public class ProxyManagerState {

    /** Proxy class to proxy mapping */
    private Map<Class, Object> proxies = [:]

    /** Proxy objects to proxy class mapping */
    private Map<Object, Class> reverseProxies = [:]

    /** Proxy class to proxy target mapping */
    private Map<Class, Object> proxyTargets = [:]

    /** Proxy target to proxy class mapping */
    private Map<Object, Class> reverseProxyTargets = [:]

    /**
     * Sets proxy object for the given interface
     *
     * @param ifc interface implemented by proxy object
     * @param obj proxy object
     */
    void setProxy(final Class ifc, final Object proxy) {
        proxies[ifc] = proxy
        reverseProxies[proxy] = ifc
    }

    /**
     * Returns proxy object for target object or interface
     *
     * @param obj target object or interface implemented by proxy
     */
    public Object getProxy(final Object obj) {
        if (obj instanceof Class) {
            proxies[obj]
        } else {
            proxies[reverseProxyTargets[obj]]
        }
    }

    /**
     * Returns currently used target object for the given interface
     * or proxy object
     *
     * @param obj proxy object or interface implemented by proxy
     */
    public Object getTarget(final Object obj) {
        if (obj instanceof Class) {
            proxyTargets[obj]
        } else {
            proxyTargets[reverseProxies[obj]]
        }
    }

    /**
     * Checks whether object or interface is a proxy
     *
     * @return whether object or interface is a proxy
     */
    public boolean isProxy(final Object obj) {
        if (obj instanceof Class) {
            proxies.containsKey(obj)
        } else {
            reverseProxies.containsKey(obj)
        }
    }

    /**
     * Sets currently used target for the proxy.
     *
     * @param ifc proxy class
     *
     * @param target currently used target
     */
    void setTarget(final Class ifc, final Object target) {
        reverseProxyTargets.remove(proxyTargets[ifc])
        proxyTargets[ifc] = target
        reverseProxyTargets[target] = ifc
    }
}
