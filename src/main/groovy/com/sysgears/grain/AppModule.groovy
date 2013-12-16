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

package com.sysgears.grain

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.assistedinject.FactoryModuleBuilder
import com.sysgears.grain.init.CmdlineOptions
import com.sysgears.grain.log.StreamLogger
import com.sysgears.grain.log.StreamLoggerFactory
import org.codehaus.groovy.control.CompilerConfiguration
import org.yaml.snakeyaml.Yaml

/**
 * IoC configuration of Grain.
 */
class AppModule extends AbstractModule {
    
    /** Command-line options */
    private CmdlineOptions options
    
    public AppModule(CmdlineOptions options) {
        this.options = options
    }

    @Provides @javax.inject.Singleton
    public Random provideRandom() {
        new Random(new Date().time)
    }

    @Provides @javax.inject.Singleton
    public Yaml provideYaml() {
        new Yaml()
    }  

    @Provides @javax.inject.Singleton
    public GroovyShell provideGroovyShell(GroovyScriptEngine engine) {
        new GroovyShell(engine.groovyClassLoader) 
    }

    @Provides @javax.inject.Singleton
    public GroovyScriptEngine provideGroovyScriptEngine() {
        ClassLoader classLoader = this.class.classLoader
        classLoader.addURL(new File(options.toolsHome, 'compass/').toURI().toURL())
        String siteDir = options.configFile.parentFile.canonicalPath
        String globalConfDir = options.globalConfigFile.parentFile.canonicalPath
        String[] gseRoots = ["${siteDir}/theme/src/", siteDir, globalConfDir]        
        def conf = new CompilerConfiguration()
        conf.setMinimumRecompilationInterval(0)
        def gcl = new GroovyClassLoader(classLoader, conf)
        GroovyScriptEngine gse = new GroovyScriptEngine(gseRoots, gcl)
        gse.setConfig(conf)
        gse
    }

    @Override
    protected void configure() {
        bind(CmdlineOptions.class).toInstance(options)
        install(new FactoryModuleBuilder()
                .implement(StreamLogger.class, StreamLogger.class)
                .build(StreamLoggerFactory.class));
    }
}
