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

/**
 * TCP/IP utils.
 */
public class TCPUtils {

    /**
     * Returns first available TCP port number from the end or null, if all ports are used. 
     * 
     * @return first available TCP port number  
     */
    public static ServerSocket getFirstAvailablePort() {
        def port = 65535

        while (port > 1024) {
            try {
                return new ServerSocket(port);
            } catch (IOException ignored) {
                port--
            } 
        }

        return null
    }

}
