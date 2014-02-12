package com.sysgears.grain.service

import com.google.inject.Inject
import groovyx.gpars.agent.Agent

/**
 * Holds dependencies between objects. 
 */
@javax.inject.Singleton
public class DependencyTracker {
    
    /** Proxy objects manager */
    @Inject private ProxyManager proxyManager

    /** Dependency tracker state agent */
    private Agent<DependencyTrackerState> state = new Agent(new DependencyTrackerState())

    /**
     * Gets dependency tracker state snapshot.
     *
     * @return dependency tracker state snapshot. 
     */
    public DependencyTrackerState getState() {
        state.val
    }

    /**
     * Adds object dependency
     * 
     * @param user object that uses supplier
     * @param supplier object that supplies services to the user
     */
    public void addLink(final Object user, final Object supplier) {
        state << { DependencyTrackerState it ->
            it.addLink(user, supplier)
        }
    }

    /**
     * Finds all objects satisfying condition
     * 
     * @param closure a closure condition
     * 
     * @return all objects satisfying condition 
     */
    public Collection findAll(final Closure closure = { true }) {
        state.val.findAll(proxyManager.state, closure)
    }

    /**
     * Finds nearest users of supplier object satisfying condition 
     *
     * @param supplier supplier object
     * @param closure a closure condition
     *
     * @return nearest users of supplier object satisfying condition 
     */
    public Collection findNearestUsers(final Object supplier, final Closure closure = { true }) {
        state.val.findNearestUsers(proxyManager.state, supplier, closure)
    }

    /**
     * Finds nearest suppliers for the user object satisfying condition 
     *
     * @param user user object
     * @param closure a closure condition
     *
     * @return nearest suppliers of user object satisfying condition 
     */
    public Collection findNearestSuppliers(final Object user, final Closure closure = { true }) {
        state.val.findNearestSuppliers(proxyManager.state, user, closure)
    }
}
