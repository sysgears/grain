package com.sysgears.grain.service

import javax.inject.Inject

/**
 * Created by victor on 2/7/14.
 */
@javax.inject.Singleton
class ExampleComponent {
    @Inject private ProviderService provider
    @Inject private MutableService mutableService
    
    public String getMessage() {
        mutableService.getMessage()
    }
}
