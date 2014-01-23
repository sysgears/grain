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

package com.sysgears.grain.deploy

import com.sysgears.grain.config.Config
import com.sysgears.grain.log.StreamLoggerFactory
import groovy.util.logging.Slf4j

import javax.inject.Inject

/**
 * Site deployer.
 */
@javax.inject.Singleton
@Slf4j
class SiteDeployer {

    /** Site config */
    @Inject private Config config

    /** Stream logger factory */
    @Inject private StreamLoggerFactory streamLoggerFactory

    /**
     * Deploys generated website.
     */
    public void deploy() {
        def deploy_cmd = config.deploy 
        Process proc = null
        if (deploy_cmd instanceof ArrayList && deploy_cmd.head() instanceof ArrayList) {
            deploy_cmd.each { cmd ->
                log.info "Executing deploy command:\n${cmd}"
                proc = cmd.execute()
                def logger =  streamLoggerFactory.create(proc.in, proc.err)
                logger.start()
                proc.waitFor()
                logger.interrupt()
                logger.join()
            }
        } else if (deploy_cmd instanceof Closure) {
            deploy_cmd()
        } else {
            log.info "Executing deploy command:\n${deploy_cmd}"
            proc = deploy_cmd.execute()
            def logger =  streamLoggerFactory.create(proc.in, proc.err)
            logger.start()
            proc.waitFor()
            logger.interrupt()
            logger.join()
        }
    }
}
