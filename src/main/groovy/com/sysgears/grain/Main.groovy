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

import com.google.inject.Guice
import com.google.inject.spi.ConstructorBinding
import com.google.inject.spi.DefaultElementVisitor
import com.sysgears.grain.compass.CompassModule
import com.sysgears.grain.rpc.python.PythonModule
import com.sysgears.grain.service.Service
import com.sysgears.grain.highlight.HighlightModule
import com.sysgears.grain.init.GrainSettings
import com.sysgears.grain.init.InitModule
import com.sysgears.grain.init.Initializer
import com.sysgears.grain.markdown.MarkdownModule
import com.sysgears.grain.preview.DisabledPreviewModule
import com.sysgears.grain.preview.PreviewModule
import com.sysgears.grain.registry.RegistryModule
import com.sysgears.grain.render.RenderModule
import com.sysgears.grain.rpc.RPCModule
import com.sysgears.grain.rpc.ruby.RubyModule
import com.sysgears.grain.rst.RstModule

/**
 * Main class of Grain application.
 * <p>
 * This class is responsible for parsing command line options,
 * initializing IoC container and handing over all the work to
 * Application bean.
 */
public class Main {
    
    /**
     * Executes Grain application
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        
        // Create init dependency injector
        def initInjector = Guice.createInjector(new InitModule())
        
        // Initialize Grain application
        def initializer = initInjector.getInstance(Initializer.class)
        GrainSettings settings = null
        try {
            settings = initializer.run(args)
        } catch (Throwable t) {
            // Exit on exception
            t.printStackTrace()
            System.exit(1)
        }

        // Create main dependency injector
        def injector = Guice.createInjector(
                new AppModule(settings),
                new RubyModule(),
                new PythonModule(),
                new RPCModule(),
                new MarkdownModule(),
                new RstModule(),
                new HighlightModule(),
                new CompassModule(),
                new RegistryModule(),
                new RenderModule(),
                settings.command in ['preview', 'generate', 'gendeploy'] ?
                    new PreviewModule() : new DisabledPreviewModule())
        
        // Run main application
        def app = injector.getInstance(Application.class)
        try {
            app.run()
        } catch (Throwable t) {
            // Exit on exception
            t.printStackTrace()
            System.exit(1)
        }
    }

}
