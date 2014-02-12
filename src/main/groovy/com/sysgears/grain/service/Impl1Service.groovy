package com.sysgears.grain.service

import groovy.util.logging.Slf4j

/**
 * Created by victor on 2/7/14.
 */
@javax.inject.Singleton
@Slf4j
class Impl1Service implements MutableService {
    
    @Override
    void start() {
        log.info("Starting service ${this.class}")
    }

    public String getMessage() {
        "${this.class} message"
    }

    @Override
    void stop() {
        log.info("Stopping service ${this.class}")
    }

    @Override
    void configChanged() {

    }
}
