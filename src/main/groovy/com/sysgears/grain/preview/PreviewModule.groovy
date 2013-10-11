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



package com.sysgears.grain.preview

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.name.Names
import com.sysgears.grain.annotations.Uncached
import com.sysgears.grain.compass.Compass
import com.sysgears.grain.config.ConfigUpdater
import com.sysgears.grain.highlight.Highlighter
import com.sysgears.grain.highlight.pygments.Pygments
import com.sysgears.grain.registry.CachedHeaderParser
import com.sysgears.grain.registry.Registry
import com.sysgears.grain.render.CachedTemplateEngine
import com.sysgears.grain.render.GrainTemplateEngine
import com.sysgears.grain.registry.URLRegistry

/**
 * Package-specific IoC config
 */
class PreviewModule extends AbstractModule {

    @Provides @javax.inject.Singleton
    public FileChangeBroadcaster provideFileChangeBroadcaster(
            CachedHeaderParser headerParser, Registry registry) {
        return new FileChangeBroadcaster(headerParser, registry)
    }

    @Provides @javax.inject.Singleton
    public ConfigChangeBroadcaster provideConfigChangeBroadcaster(
            ConfigUpdater configUpdater, Highlighter highlighter,  @Uncached Highlighter uncachedHighlighter,
            Pygments pygments, Compass compass) {
        return new ConfigChangeBroadcaster(configUpdater, highlighter, uncachedHighlighter, pygments, compass)
    }

    @Provides @javax.inject.Singleton
    public SiteChangeBroadcaster provideSiteChangeBroadcaster(
            GrainTemplateEngine templateEngine, CachedTemplateEngine cachedTemplateEngine,
            URLRegistry urlRegistry) {
        return new SiteChangeBroadcaster(templateEngine, cachedTemplateEngine, urlRegistry)
    }

    @Override
    protected void configure() {
        bind(Object.class).annotatedWith(Names.named("renderMutex")).toInstance(new Object())
    }
}
