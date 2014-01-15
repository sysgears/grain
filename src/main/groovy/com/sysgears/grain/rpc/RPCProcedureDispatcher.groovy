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

import groovyx.gpars.actor.DefaultActor

/**
 * Class that treats missing method invocations as RPC calls
 */
class RPCProcedureDispatcher {
    
    /** RPC executor */
    private final DefaultActor executor
    
    /** Module name */
    private String moduleName

    /**
     * Creates an instance of the class.
     *
     * @param executor RPC executor 
     * @param moduleName module name to which RPC functions belong
     */
    public RPCProcedureDispatcher(final DefaultActor executor, final String moduleName) {
        this.executor = executor
        this.moduleName = moduleName
    }

    /**
     * Intercepts method calls and transfer them to RPC executor.
     *
     * @param method method to be called
     * @param args method arguments
     *
     * @return return value
     */
    public def methodMissing(final String method, args) {
        executor.sendAndWait(new RPCCall(moduleName: moduleName,
                procName: method,
                args: args as List<String>))  
    }
}
