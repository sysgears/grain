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
import groovy.util.logging.Slf4j
import groovyx.gpars.actor.DefaultActor

import javax.inject.Inject

/**
 * Executes RPC calls synchronously by sending request and receiving replies over pipes. 
 */
@Slf4j
class RPCExecutor extends DefaultActor {

    /** IPC input stream */
    private DataInputStream is

    /** IPC output stream */
    private DataOutputStream os

    /**
     * Creates RPC executor instance
     *
     * @param is IPC input stream
     * @param os IPC output stream
     */
    @Inject
    public RPCExecutor(@Assisted final InputStream is,
                       @Assisted final OutputStream os) {
        this.is = new DataInputStream(is)
        this.os = new DataOutputStream(os)
    }

    /**
     * Executes RPC calls and replies with return values.
     */
    public void act() {
        loop {
            react { RPCCall call ->
                log.trace("Executing RPC call ${call.moduleName}.${call.procName}(${call.args})")
                writeString(os, call.moduleName)
                writeString(os, call.procName)
                writeInt(os, call.args.size())
                for (Object arg: call.args) {
                    writeString(os, arg.toString())
                }
                os.flush()
                def result = readString(is)
                log.trace("Finished executing RPC call ${call.moduleName}.${call.procName}")
                reply result
            }
        }
    }

    /**
     * Writes integer to binary stream  
     *
     * @param out output stream
     * @param i integer
     */
    private static void writeInt(DataOutputStream out, Integer i) {
        out.writeInt(i)
    }

    /**
     * Writes string to binary stream  
     *
     * @param out output stream
     * @param i string
     */
    private static void writeString(DataOutputStream out, String str) {
        out.writeInt(str.bytes.length)
        out.write(str.bytes)
    }

    /**
     * Reads string from binary stream
     *
     * @param is input stream
     *
     * @return string
     */
    private static String readString(DataInputStream is) {
        def len = is.readInt()
        if (len == -1) {
            return null
        }
        def bytes = new byte[len]
        is.readFully(bytes)
        new String(bytes)
    }    
}
