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

package com.sysgears.grain.config

import com.sysgears.grain.init.GrainSettings
import com.sysgears.grain.preview.ConfigChangeListener
import com.sysgears.grain.taglib.Site
import groovy.util.logging.Slf4j

import javax.inject.Inject
import javax.inject.Named

/**
 * Listens for file system changes in config file and updates config accordingly
 */
@Named
@javax.inject.Singleton
@Slf4j
class ConfigUpdater implements ConfigChangeListener {

    /** Grain settings */
    @Inject private GrainSettings settings

    /** Config parser */
    private ConfigSlurper configParser

    /** Groovy script engine to execute config */
    @Inject private GroovyScriptEngine gse

    /**
     * Bindings passed to config code.
     */
    private Map defaultConfigBindings

    /** Site config */
    @Inject Config config

    /**
     * Creates an instance of Site's config
     *
     * @param settings Grain settings
     * @param site site instance
     */
    @Inject
    public ConfigUpdater(Site site, GrainSettings settings) {
        this.settings = settings
        this.configParser = new ConfigSlurper(settings.env)
        this.defaultConfigBindings = ["site": site, "log": log]
    }

    /**
     * Rereads config when it is modified on the filesystem.
     */
    public void configChanged() {
        ConfigObject newConfig = new ConfigObject()

        for (configFile in [settings.globalConfigFile, settings.configFile]) {
            if (configFile.exists()) {
                log.info "Rereading config ${configFile}"
                configParser.binding = newConfig + defaultConfigBindings
                def config = gse.loadScriptByName(configFile.name)
                newConfig = configParser.parse(config).merge(newConfig) as ConfigObject
            }
        }

        newConfig.putAll(defaultConfigBindings)
        config.reload(newConfig)
        if (config.config_posthandler) {
            log.info "Executing config post handler"
            config.config_posthandler.call()
        }
    }
}
