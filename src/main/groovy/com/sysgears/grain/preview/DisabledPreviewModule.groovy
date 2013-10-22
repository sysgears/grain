package com.sysgears.grain.preview

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.name.Names
import com.sysgears.grain.config.ConfigUpdater

/**
 * The IoC module that is used for the commands that have no preview. 
 */
class DisabledPreviewModule extends AbstractModule {

    @Provides @javax.inject.Singleton
    public FileChangeBroadcaster provideFileChangeBroadcaster() {
        return new FileChangeBroadcaster()
    }

    @Provides @javax.inject.Singleton
    public ConfigChangeBroadcaster provideConfigChangeBroadcaster(
            ConfigUpdater configUpdater) {
        return new ConfigChangeBroadcaster(configUpdater)
    }

    @Provides @javax.inject.Singleton
    public SiteChangeBroadcaster provideSiteChangeBroadcaster() {
        return new SiteChangeBroadcaster()
    }

    @Override
    protected void configure() {
        bind(Object.class).annotatedWith(Names.named("renderMutex")).toInstance(new Object())
    }
    
}
