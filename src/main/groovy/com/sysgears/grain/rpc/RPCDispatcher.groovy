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

package com.sysgears.grain.rpc

import com.google.inject.assistedinject.Assisted
import groovyx.gpars.actor.DefaultActor

import javax.inject.Inject

/**
 * Remote procedure calls dispatcher.
 * 
 * Dispatches RPC calls to the Ruby or Python RPC executor.  
 */
public class RPCDispatcher {
    
    /** RPC executor */
    private DefaultActor executor

    /** 
     * Creates instance of RPC dispatcher 
     * 
     * @param executor an RPC executor which should be used by this dispatcher
     */
    @Inject
    public RPCDispatcher(@Assisted DefaultActor executor) {
        this.executor = executor
    }

    /**
     * Intercepts missing property as RPC module name
     * and instantiate RPC object that will treat missing method invocations as RPC calls
     *
     * @param name Class name for Ruby or Module name for Python
     *
     * @return RPC object
     */
    def propertyMissing(final String name) {
        new RPCProcedureDispatcher(executor, name)
    }    
}
