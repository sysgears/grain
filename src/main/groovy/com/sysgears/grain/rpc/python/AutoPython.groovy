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

package com.sysgears.grain.rpc.python

import com.sysgears.grain.annotations.Spare
import com.sysgears.grain.rpc.RPCDispatcher
import com.sysgears.grain.rpc.ruby.Ruby

import javax.inject.Inject

/**
 * Service which launches available flavor of Python (CPython of Jython) in the system 
 */
@javax.inject.Singleton
public class AutoPython implements Ruby {

    /** Python command finder */
    @Inject private PythonFinder pythonFinder

    /** CPython implementation */
    @Inject private CPython cPython

    /** Jython implementation */
    @Inject private Jython jython

    /** Python implementation service used */
    private Python python

    /**
     * Starts the service.
     */
    public void start() {
        python = pythonFinder.cmd != null ? cPython : jython
        
        python.start()
    }

    /**
     * Stops the service.
     */
    public void stop() {
    }

    /**
     * Return RPC implementation.
     */
    public RPCDispatcher getRpc() {
        if (!python) {
            start()
        }
        python.rpc
    }

    @Override
    void configChanged() {
    }
}
