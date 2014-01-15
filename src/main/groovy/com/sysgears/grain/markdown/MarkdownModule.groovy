package com.sysgears.grain.markdown

import com.google.inject.AbstractModule
import com.google.inject.Injector
import com.google.inject.Provides
import com.sysgears.grain.config.Config
import com.sysgears.grain.config.ImplBinder

/**
 * Package-specific IoC config
 */
class MarkdownModule extends AbstractModule {
    
    @Provides @javax.inject.Singleton
    public MarkdownProcessor provideProcessor(Injector injector,
                                  TxtMarkProcessor txtmark,
                                  PegdownProcessor pegdown) {
        new ImplBinder<MarkdownProcessor>(MarkdownProcessor.class, 'features.markdown',
                [default: txtmark, txtmark: txtmark, pegdown: pegdown], injector).proxy
    }

    @Override
    protected void configure() {
    }

}