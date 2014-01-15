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

package com.sysgears.grain.service

import com.google.inject.Injector
import com.sysgears.grain.annotations.Manual
import groovy.util.logging.Slf4j

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock

import static groovyx.gpars.GParsPool.withPool
import static groovyx.gpars.GParsPool.executeAsyncAndWait
import static groovyx.gpars.dataflow.Dataflow.task
import groovyx.gpars.dataflow.DataflowQueue

import javax.inject.Inject

/**
 * Manages Grain service dependencies.
 */
@Slf4j
@javax.inject.Singleton
public class ServiceManager implements Service {

    /**
     * Service agent protects service start and stop method in the following way:
     * if the service is already starting the start method blocks until the service and all
     * the services it depends on will be completely started, the same is true for stop method. 
     */
    private class ServiceAgent {

        /** Target service */
        private Service target

        /** Lock to access service state */
        private def serviceLock = new ReentrantLock()

        /** Whether this service was stopped due to some other service was stopping */
        private AtomicBoolean chained = new AtomicBoolean(false)
        
        /** Whether this service is running */
        private boolean running

        /**
         * Creates an instance of this class
         *
         * @param target target service
         */
        public ServiceAgent(final Service target) {
            this.target = target
        }

        /**
         * Starts this service and all the services it depends on.
         * 
         * This method blocks until this service and all the services it depends on will be started.
         */
        public void start() {
            managerLock.readLock().lock()
            try {
                serviceStart(false)
            } finally {
                managerLock.readLock().unlock()
            }
        }

        /**
         * Starts this service and all the services it depends on.
         *
         * This method blocks until this service and all the services it depends on will be started.
         * 
         * @param chained - whether we should start this service only in case it was stopped due to
         *                  other service was stopping
         */
        public void serviceStart(boolean chained) {
            log.trace "Attempt to start service ${target.class}"
            boolean serviceStarted = false
            try {
                if (!chained || this.chained.get()) {
                    serviceLock.lock()
                    try {
                        if (!running) {
                            log.trace "Starting services that are used by ${target.class}..."
                            withPool {
                                providers[this]?.eachParallel {
                                    log.trace "${target.class} waits for service provider ${it.target.class} to start"
                                    it.serviceStart(false)
                                }
                            }
                            log.debug "Service ${target.class} starting..."
                            target.originalStart()
                            running = true
                            log.debug "Service ${target.class} started."
                            
                            serviceStarted = true
                        } else {
                            log.debug "Service ${target.class} is already started"
                        }
                    } finally {
                        serviceLock.unlock()
                    }
                } else {
                    log.trace "Service ${target.class} ignored to start."
                }
                if (serviceStarted) {
                    log.trace "Attempting to start autostopped users of service ${target.class}..."
                    withPool {
                        users[this]?.eachParallel {
                            log.trace "${target.class} checks service user ${it.target.class} to start"
                            it.serviceStart(true)
                        }
                    }
                }
            } catch (e) {
                log.error("Error starting service", e)
            } 
        }

        /**
         * Stops all the services depending on this service and then the service itself.
         *
         * This method blocks until this service and all the dependent services will be stopped.
         *
         * @param chained - whether chained stop is in progress and this service is asked
         *                  to stop
         */
        public void serviceStop(boolean chained) {
            log.trace "Attempt to stop service ${target.class}"
            serviceLock.lock()
            try {
                if (running) {
                    log.trace "Stopping services that use ${target.class}..."
                    withPool {
                        users[this]?.eachParallel {
                            log.trace "${target.class} waits for service ${it.target.class} to stop"
                            it.serviceStop(true)
                        }
                    }
                    
                    log.debug "Service ${target.class} stopping..."
                    if (!target.class.toString().startsWith('class $Proxy')) {
                        target.originalStop()
                    }
                    this.running = false
                    this.chained.set(chained)
                    log.debug "Service ${target.class} stopped."
                } else {
                    log.debug "Service ${target.class} is already stopped"
                }
            } catch (e) {
                log.error("Error stopping service", e)
            } finally {
                serviceLock.unlock()
            }
        }

        /**
         * Stops all the services depending on this service and then the service itself.
         *
         * This method blocks until this service and all the dependent services will be stopped.
         */
        public void stop() {
            managerLock.readLock().lock()
            try {
                serviceStop(false)
            } finally {
                managerLock.readLock().unlock()
            }
        }
    }
    
    /** Guice injector */
    @Inject private Injector injector
    
    /** What services should be started when this service is going to start */
    private Map<ServiceAgent, Set<ServiceAgent>> users = [:]

    /** What services should be stopped when this service is going to stop */
    private Map<ServiceAgent, Set<ServiceAgent>> providers = [:]

    /** Map of services and their agents */
    private Map<Service, ServiceAgent> services = [:]
    
    /** Lock to access manager state */
    private def managerLock = new ReentrantReadWriteLock()
    
    /** Runtime service dependency */
    private class RuntimeDependency {
        Service provider
        Service user
    }
    
    /** Queue to register runtime dependencies */
    private DataflowQueue<RuntimeDependency> depQueue = new DataflowQueue<RuntimeDependency>()

    /**
     * @inheritDoc
     */
    @Override
    public void start() {
        log.info "Starting Service Manager..."

        Service.metaClass.start = {}
        Service.metaClass.stop = {}

        // Discover all the services we have and wrap each of them into service agent
        discoverAllServices()

        // Build service dependency tree
        buildStaticDependencies()
        
        // Handle runtime service registrations
        task {
            while (!Thread.currentThread().interrupted) {
                def reg = depQueue.val
                registerRuntimeDependency(reg.provider, reg.user)
            }
        }
        
        addShutdownHook { this.stop() }
        
        log.info "Service Manager started."
    }

    /**
     * Dumps dependency trees into log to aid deep debugging.
     */
    public void dumpDependencyTrees() {
        managerLock.readLock().lock()
        try {
            log.debug "Start dependencies:"
            providers.each { svc, deps ->
                log.info "    ${svc.target.class} uses:"
                deps.each {
                    log.info "        ${it.target.class}"
                }
            }

            log.debug "Stop dependencies:"
            users.each { svc, deps ->
                log.info "    ${svc.target.class} is used by:"
                deps.each {
                    log.info "        ${it.target.class}"
                }
            }
        } finally {
            managerLock.readLock().unlock()
        }
    }
    
    /**
     * Discovers all the services we have through Guice injector and wraps each service
     * into protecting agent.
     */
    private void discoverAllServices() {
        injector.allBindings.each { key, value ->
            def obj = injector.getInstance(key)
            if (obj != null && obj.class != null && obj != this &&
                    Service.class.isAssignableFrom(obj.class)) {
                registerService(obj as Service)
            }
        }
    }

    /**
     * Registers service and returns its agent. 
     * 
     * @param service service to register
     * 
     * @return service agent
     */
    private ServiceAgent registerService(final Service service) {
        managerLock.writeLock().lock()
        try {
            def agent = services[service]
            if (!agent) {
                log.trace "Registering service: ${service.class}"
                agent = new ServiceAgent(service)
                service.metaClass.originalStart = {
                    log.trace "${delegate.class}.originalStart()"
                    delegate.&start()
                }
                service.metaClass.originalStop = {
                    log.trace "${delegate.class}.originalStop()"
                    delegate.&stop()
                }
                service.metaClass.start = {
                    agent.start()
                }
                service.metaClass.stop = {
                    agent.stop()
                }
    
                services[service] = agent
                users[agent] = [] as Set
                providers[agent] = [] as Set
            }
            
            agent
        } finally {
            managerLock.writeLock().unlock()
        }
    }

    /**
     * Builds service dependency trees for two cases:
     * 1. For starting each service
     * 2. For stopping each service 
     */
    private void buildStaticDependencies() {
        managerLock.writeLock().lock()
        try {
            services.each { svc, userAgent ->
                svc.class.declaredFields.each {
                    if (Service.class.isAssignableFrom(it.type) && it.isAnnotationPresent(Inject.class)) {
                        def obj = injector.getInstance(it.type)
                        if (svc != obj) {
                            def provAgent = services[obj as Service]
                            log.debug svc.class.toString() + " uses " + obj.class
                            if (!it.isAnnotationPresent(Manual.class)) {
                                providers[userAgent].add(provAgent)
                                users[provAgent].add(userAgent)
                            }
                        }
                    }
                }
            }
        } finally {
            managerLock.writeLock().unlock()
        }
    }

    /**
     * Adds dependency between services at runtime.
     *
     * @param provider a service
     * @param user service that uses provider
     */
    public void addRuntimeDependency(final Service provider, final Service user) {
        depQueue << new RuntimeDependency(provider: provider, user: user)
    }

    /**
     * Adds dependency between services at runtime.
     * 
     * @param provider a service
     * @param user service that uses provider
     */   
    public void registerRuntimeDependency(final Service provider, final Service user) {
        managerLock.writeLock().lock()
        try {
            if (provider != user) {
                def provAgent = services[provider] ?: registerService(provider)
                def userAgent = services[user] ?: registerService(user)

                log.debug user.class.toString() + " at runtime uses " + provider.class
                users[provAgent].add(userAgent)
            }
        } finally {
            managerLock.writeLock().unlock()
        }
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public void stop() {
        log.info "Stopping Service Manager..."
        managerLock.readLock().lock()
        try {
            withPool {
                services.values().eachParallel {
                    it.stop()
                }
            }
        } finally {
            managerLock.readLock().unlock()
        }
        log.info "Service Manager stopped."
    }
}
