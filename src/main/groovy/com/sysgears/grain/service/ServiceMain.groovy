package com.sysgears.grain.service

import com.google.inject.Guice

/**
 * Created by victor on 2/7/14.
 */
class ServiceMain {

    public static void main(String[] args) {
        // Create main dependency injector
        def injector = Guice.createInjector(new TestModule())
        
        injector.getInstance(ServiceApp).run()
    }
    
}
