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



package com.sysgears.grain.render

import com.google.inject.AbstractModule
import com.google.inject.assistedinject.FactoryModuleBuilder
import com.sysgears.grain.annotations.Uncached

/**
 * Package-specific IoC config 
 */
class RenderModule extends AbstractModule {
    
    @Override
    protected void configure() {
        install(new FactoryModuleBuilder()
                .implement(RawTemplate.class, RawTemplate.class)
                .build(RawTemplateFactory.class))

        install(new FactoryModuleBuilder()
                .implement(GroovyTemplate.class, GroovyTemplate.class)
                .build(GroovyTemplateFactory.class))

        install(new FactoryModuleBuilder()
                .implement(TextTemplate.class, TextTemplate.class)
                .build(TextTemplateFactory.class))

        bind(TemplateEngine.class).to(CachedTemplateEngine.class)
        bind(TemplateEngine.class).annotatedWith(Uncached.class).to(GrainTemplateEngine.class)
    }
}
