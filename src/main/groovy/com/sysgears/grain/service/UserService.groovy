package com.sysgears.grain.service

import groovy.util.logging.Slf4j

import javax.inject.Inject

/**
 * Created by victor on 2/7/14.
 */
@javax.inject.Singleton
@Slf4j
class UserService implements Service {
    
    @Inject ExampleComponent component

    @Override
    void start() {
        log.info("Starting service ${this.class}")
    }
    
    public String getMessage() {
        component.getMessage()
    }

    @Override
    void stop() {
        log.info("Stopping service ${this.class}")
    }
}
