package com.sysgears.grain.rst

import com.google.inject.AbstractModule
import com.google.inject.Injector
import com.google.inject.Provides
import com.sysgears.grain.config.ImplBinder

/**
 * Package-specific IoC config
 */
class RstModule extends AbstractModule {
    
    @Provides @javax.inject.Singleton
    public RstProcessor provideProcessor(Injector injector) {
        new ImplBinder<RstProcessor>(RstProcessor.class, 'features.rst',
                [default: null], injector).proxy
    }

    @Override
    protected void configure() {
    }

}