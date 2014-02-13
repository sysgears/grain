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

package com.sysgears.grain.rpc.ruby

import com.sysgears.grain.annotations.Spare
import com.sysgears.grain.rpc.RPCDispatcher

import javax.inject.Inject

/**
 * Service which launches available flavor of Ruby (RMI of JRuby) in the system 
 */
@javax.inject.Singleton
public class AutoRuby implements Ruby {

    /** Ruby command finder */
    @Inject private RubyFinder rubyFinder

    /** RMI Ruby implementation */
    @Inject private RMIRuby rmiRuby

    /** JRuby implementation */
    @Inject private JRuby jruby

    /** Ruby implementation service used */
    private Ruby ruby

    /**
     * Starts the service.
     */
    public void start() {
        ruby = rubyFinder.cmd != null ? rmiRuby : jruby
        
        ruby.start()
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
        if (!ruby) {
            start()
        }
        ruby.rpc
    }

    @Override
    void configChanged() {
    }
}
