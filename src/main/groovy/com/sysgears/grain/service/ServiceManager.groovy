package com.sysgears.grain.service

import com.google.inject.Injector
import groovy.util.logging.Slf4j
import groovyx.gpars.agent.Agent
import static groovyx.gpars.dataflow.Dataflow.task

import javax.inject.Inject

import static groovyx.gpars.GParsPool.withPool

/**
 * Manages Grain service dependencies.
 */
@Slf4j
@javax.inject.Singleton
public class ServiceManager implements Service {

    /** Service state */
    private static class ServiceState {

        /** Service is running */
        boolean running

        /** Service was stopped as a result of other service stop */
        boolean chained

        /** Service was shutdown, it will not respond to start */
        boolean shutdown
    }

    /**
     * Service agent protects service in the following way: if any method is called on a service
     * it attempts to fully start first and then execute the method,
     * if the service is already starting the start method blocks until the service and all
     * the services it depends on will be completely started, the same is true for stop method. 
     */
    private class ServiceAgent {
        
        /**
         * Waits for service operations and warns if they take too long.
         */
        private class AlarmThread extends Thread {

            /** Warning message */
            private final String warnMsg
            
            /** Alarm time in ms */
            private final Long alarmTime

            /**
             * Creates instance of alarm thread 
             * 
             * @param warnMsg warning message
             * @param alarmTime alarm time in ms
             */
            public AlarmThread(final String warnMsg, final Long alarmTime) {
                setDaemon(true)
                this.warnMsg = warnMsg
                this.alarmTime = alarmTime
            }

            /**
             * Waits for service operations and warns if they take too long. 
             */
            public void run() {
                boolean exit = false
                long waitTime = 0
                while (!exit) {
                    sleep(alarmTime) { exit = true }
                    waitTime += alarmTime
                    if (!exit)
                        log.warn("${warnMsg} after ${waitTime} ms")
                }
            }
        }
        
        /** Target service */
        private Service target
        
        /** Service state agent */
        private Agent<ServiceState> state = new Agent(new ServiceState())

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
         *
         * @param chained - whether we should start this service only in case it was stopped due to
         *                  other service was stopping
         */
        public void serviceStart(final ServiceState state, final boolean chained) {
            log.trace "Attempt to start service ${target.class}"

            if (!state.shutdown && (!chained || state.chained)) {
                if (!state.running) {
                    log.debug "Service ${target.class} starting..."
                    def alarm = new AlarmThread("Still waiting for service ${target.class} to start", 30000L)
                    alarm.start()
                    invokeOriginal('start')
                    alarm.interrupt()
                    state.running = true

                    task {
                        def trackerState = tracker.state
                        def proxyState = proxyManager.state

                        log.trace "Attempting to start autostopped users of service ${target.class}..."
                        withPool {
                            trackerState.findNearestUsers(proxyState, target, serviceClosure).eachParallel { Service service ->
                                log.trace "${target.class} checks service user ${service.class} to start"
                                service.chainedStart()
                            }
                        }
                    }
                    log.debug "Service ${target.class} started."
                } else {
                    log.debug "Service ${target.class} is already started"
                }
            }
        }

        /**
         * Stops all the services depending on this service and then the service itself.
         *
         * This method blocks until this service and all the dependent services will be stopped.
         *
         * @param chained - whether chained stop is in progress and this service is asked
         *                  to stop
         * @param shutdown - whether service should be shut down and never started again  
         */
        public void serviceStop(final ServiceState state, final boolean chained, final boolean shutdown) {
            log.trace "Attempt to stop service ${target.class}"
            def trackerState = tracker.state
            def proxyState = proxyManager.state

            if (state.running) {
                log.trace "Stopping services that use ${target.class}..."
                withPool {
                    trackerState.findNearestUsers(proxyState, target, serviceClosure).eachParallel { Service service ->
                        log.trace "${target.class} waits for service ${service.class} to stop"
                        if (shutdown) {
                            service.stopAndShutdown()
                        } else {
                            service.chainedStop()
                        }
                    }
                }

                log.debug "Service ${target.class} stopping..."
                def alarm = new AlarmThread("Still waiting for service ${target.class} to stop", 10000L)
                alarm.start()
                invokeOriginal('stop')
                alarm.interrupt()
                state.running = false
                state.chained = chained
                state.shutdown = shutdown
                log.debug "Service ${target.class} stopped."
            } else {
                log.debug "Service ${target.class} is already stopped"
            }
        }
        
        /**
         * Handles method calls on a service
         * 
         * @param name method name
         * @param args method args
         * 
         * @return return value
         */
        public Object handleMethod(String name, args) {
            def result = Void
            //log.trace "Before ${target.class}.${name}"
            if (name in ['stop', 'chainedStop', 'stopAndShutdown']) {
                state.sendAndWait { ServiceState it ->
                    try {
                        serviceStop(it, name == 'chainedStop', name ==  'stopAndShutdown')
                    } catch (e) {
                        new AgentError(cause: e)
                    }
                }
            } else {
                result = state.sendAndWait { ServiceState it ->
                    try {
                        serviceStart(it, name == 'chainedStart')
                        if (!(name in ['start', 'chainedStart']))
                            invokeOriginal(name, args)
                    } catch (e) {
                        new AgentError(cause: new RuntimeException("While executing ${target.class}.${name}", e))
                    }
                }
            }
            if (result instanceof AgentError)
                throw result.cause
            //log.trace "After ${target.class}.${name}"
            result
        }

        /**
         * Invokes original method of the service
         *
         * @param name method name
         * @param args method args
         *
         * @return return value
         */
        private Object invokeOriginal(String name, args = [] as Object[]) {
            def metaMethod = target.metaClass.getMetaMethod(name, args)
            metaMethod.invoke(target, args)
        }
    }

    /** Guice injector */
    @Inject private Injector injector

    /** Tracks dependencies between objects */
    @Inject private DependencyTracker tracker

    /** Proxy manager */
    @Inject private ProxyManager proxyManager

    /** Tracks dependencies between singletons */
    @Inject private GuiceSingletonDiscoverer discoverer

    private Agent<ServiceState> state = new Agent(new ServiceState())

    /** Closure to select all services except ourself */
    private def serviceClosure = { it.class != ServiceManager && Service.class.isAssignableFrom(it.class) }

    /**
     * Starts service manager.
     */
    @Override
    public void start() {
        def result = state.sendAndWait { ServiceState it ->
            try {
                if (!it.running) {
                    log.debug "Starting Service Manager..."

                    // Build static dependency tree
                    discoverer.discover()

                    // Protect all services by wrapping each of them into ServiceAgent
                    protectServices()

                    addShutdownHook { this.stop() }

                    it.running = true
                    log.debug "Service Manager started."
                }
            } catch (e) {
                new AgentError(cause: e)
            }
        }
        if (result instanceof AgentError)
            throw result.cause
    }
    
    /*
     * Protects all services by wrapping each of them into ServiceAgent
     */
    private void protectServices() {
        allServices.each { Service it ->
            def agent = new ServiceAgent(it)

            it.metaClass.invokeMethod = { String name, args ->
                def protectMethod = !(name in ['asBoolean', 'asType', 'equals', 'hashCode',
                        'invokeMethod', 'configChanged'])
                if (protectMethod) {
                    agent.handleMethod(name, args)
                } else {
                    args ? delegate.&"${name}"(args) : delegate.&"${name}"() 
                }
            }
        }
    }

    /**
     * Retrieves list of all the services.
     * 
     * @return list of all the service
     */
    private Set<Service> getAllServices() {
        tracker.findAll(serviceClosure) as Set<Service>
    }

    /**
     * Stops service manager.
     */
    @Override
    public void stop() {
        def result = state.sendAndWait { ServiceState it ->
            try {
                if (it.running) {
                    log.debug "Stopping Service Manager..."
                    withPool {
                        allServices.eachParallel {
                            it.stopAndShutdown()
                        }
                    }
                    log.debug "Service Manager stopped."
    
                    it.running = false
                }
            } catch (e) {
                new AgentError(cause: e)
            }
        }
        if (result instanceof AgentError)
            throw result.cause
    }

    /**
     * Dumps dependency trees of singletons implementing Service interface
     * into log to aid deep debugging.
     */
    public void dumpServiceTree() {
        log.debug "Service dependencies:"
        allServices.each { svc ->
            log.info "    ${svc.class} is used by:"
            def deps = tracker.findNearestUsers(svc, serviceClosure)

            deps.each {
                log.info "        ${it.class}"
            }
        }
    }
}
