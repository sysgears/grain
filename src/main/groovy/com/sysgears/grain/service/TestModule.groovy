package com.sysgears.grain.service

import com.google.inject.AbstractModule
import com.google.inject.Provides

/**
 * Created by victor on 2/10/14.
 */
class TestModule extends AbstractModule {

    @Provides @javax.inject.Singleton
    public MutableService provideMutableService(ProxyManager manager) {
        manager.createProxy(MutableService)
    }

    @Override
    protected void configure() {
    }
}
