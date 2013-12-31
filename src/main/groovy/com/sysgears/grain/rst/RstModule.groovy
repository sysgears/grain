package com.sysgears.grain.rst

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.sysgears.grain.config.Config
import com.sysgears.grain.config.ImplBinder
import com.sysgears.grain.markdown.MarkdownProcessor
import com.sysgears.grain.markdown.PegdownProcessor
import com.sysgears.grain.markdown.TxtMarkProcessor

/**
 * Package-specific IoC config
 */
class RstModule extends AbstractModule {
    
    @Provides @javax.inject.Singleton
    public RstProcessor provideProcessor(Config config,
                                  JRstProcessor jrst) {
        new ImplBinder<RstProcessor>(RstProcessor.class, config, 'features.rst',
                [default: jrst, jrst: jrst]).proxy
    }

    @Override
    protected void configure() {
    }

}