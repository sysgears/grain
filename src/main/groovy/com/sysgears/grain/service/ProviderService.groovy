package com.sysgears.grain.service

import com.google.inject.Inject
import groovy.util.logging.Slf4j

/**
 * Created by victor on 2/7/14.
 */
@javax.inject.Singleton
@Slf4j
class ProviderService implements Service {
    
    @Override
    void start() {
        log.info("Starting service ${this.class}")
    }

    @Override
    void stop() {
        log.info("Stopping service ${this.class}")
    }
}
