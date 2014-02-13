package com.sysgears.grain.service

/**
 * Proxy manager state implementation.
 */
public class ProxyManagerState {

    /** Proxy class to proxy target mapping */
    private Map<Object, Object> proxyTargets = [:]

    /** Proxy target to proxy class mapping */
    private Map<Object, Object> reverseProxyTargets = [:]

    /**
     * Returns proxy object for given target object
     *
     * @param proxy proxy object
     */
    public Object getProxy(final Object target) {
        reverseProxyTargets[target]
    }

    /**
     * Returns currently used target object for the given proxy object
     *
     * @param proxy proxy object or interface implemented by proxy
     */
    public Object getTarget(final Object proxy) {
        proxyTargets[proxy]
    }

    /**
     * Checks whether object is a proxy
     *
     * @return whether object is a proxy
     */
    public boolean isProxy(final Object proxy) {
        proxyTargets.containsKey(proxy)
    }

    /**
     * Sets currently used target for the proxy.
     *
     * @param obj proxy object
     *
     * @param target currently used target
     */
    void setTarget(final Object proxy, final Object target) {
        proxyTargets[proxy] = target
        reverseProxyTargets[target] = proxy
    }
}
