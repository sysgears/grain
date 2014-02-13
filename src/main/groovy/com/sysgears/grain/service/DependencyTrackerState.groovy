package com.sysgears.grain.service

/**
 * Implementation of dependency tracker state.
 */
public class DependencyTrackerState {

    /** All objects set */
    private Set<Object> objects = []

    /** What objects are using key object */
    private Map<Object, Set<Object>> users = [:]

    /** What objects are used by key object */
    private Map<Object, Set<Object>> usedBy = [:]

    /**
     * Adds object dependency
     *
     * @param user object that uses supplier
     * @param supplier object that supplies services to the user
     */
    void addLink(final Object user, final Object supplier) {
        if (user == null || supplier == null) {
            throw new IllegalArgumentException("Null objects are disallowed")
        }
        if (!users[supplier]) users[supplier] = [] as Set
        if (!usedBy[user]) usedBy[user] = [] as Set

        objects.add(user)
        objects.add(supplier)

        users[supplier].add(user)
        usedBy[user].add(supplier)
    }

    /**
     * Finds all objects satisfying condition
     *
     * @param closure a closure condition
     *
     * @return all objects satisfying condition 
     */
    Collection findAll(final ProxyManagerState proxyState, final Closure closure = { true }) {
        objects.findAll { !proxyState.isProxy(it) }.findAll(closure)
    }

    /**
     * Finds nearest users of supplier object satisfying condition 
     *
     * @param proxyState proxy manager state
     * @param closure a closure condition
     * @param visited suppliers already visited
     *
     * @return nearest users of supplier object satisfying condition 
     */
    Set findNearestUsers(final ProxyManagerState proxyState, final Object supplier,
                         final Closure closure = { true },
                         final Set<Object> visited = [] as Set) {
        if (supplier == null) {
            throw new IllegalArgumentException("Null objects are disallowed")
        }
        users[getProxy(proxyState, supplier)]?.collect {
            if (closure(getTarget(proxyState, it))) {
                getTarget(proxyState, it)
            } else {
                if (!visited.contains(it)) {
                    findNearestUsers(proxyState, it, closure, (visited + supplier) as Set)
                } else {
                    null
                }
            }
        }?.flatten()?.findAll() ?: [] as Set
    }

    /**
     * Finds nearest suppliers for the user object satisfying condition 
     *
     * @param proxyState proxy manager state
     * @param closure a closure condition
     * @param visited users already visited
     *
     * @return nearest suppliers of user object satisfying condition 
     */
    Set findNearestSuppliers(final ProxyManagerState proxyState,
                             final Object user, final Closure closure = { true },
                             final Set<Object> visited = [] as Set) {
        if (user == null) {
            throw new IllegalArgumentException("Null objects are disallowed")
        }
        usedBy[getProxy(proxyState, user)]?.collect {
            if (closure(getTarget(proxyState, it))) {
                getTarget(proxyState, it)
            } else {
                if (!visited.contains(it)) {
                    findNearestSuppliers(proxyState, it, closure, (visited + user) as Set)
                } else {
                    null
                }
            }
        }?.flatten()?.findAll() ?: [] as Set
    }

    /**
     * Gets target object for proxy
     *
     * @param proxyState proxy manager state snapshot 
     * @param obj target object, or original object if obj is not a proxy
     *
     * @return real object
     */
    private static Object getTarget(final ProxyManagerState proxyState, final Object obj) {
        def result = proxyState.getTarget(obj) ?: obj
//        println "GetTarget: ${obj} - ${proxyState.getTarget(obj)}"
        result
    }

    /**
     * Gets proxy object
     *
     * @param proxyState proxy manager state snapshot 
     * @param obj target object
     *
     * @return proxy object or original object if this obj is not bound to any proxy 
     */
    private static Object getProxy(final ProxyManagerState proxyState, final Object obj) {
        def result = proxyState.getProxy(obj) ?: obj
//        println "GetProxy: ${obj} - ${proxyState.getProxy(obj)}"
        result
    }
    
}
