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

package com.sysgears.grain.log

import com.google.inject.assistedinject.Assisted
import groovy.util.logging.Slf4j

import javax.inject.Inject
import javax.inject.Named

/**
 * Interruptible unblocking multiple stream logger
 */
@Slf4j
class StreamLogger {
    
    /** Read buffer size  */
    private static final int BUFFER_SIZE = 4096
    
    /** Streams to log */
    private List<InputStream> streams
    
    /** Stream logging threads */
    private List<Thread> threads
    
    /**
     * Creates an instance of the class
     * 
     * @param streams list of input streams to read from and log
     */
    @Inject
    public StreamLogger(@Assisted InputStream... streams) {
        this.streams = streams
    }

    /**
     * Reads from list of input streams and outputs to the log until thread will be interrupted
     * or stream will be closed on the other end.
     */
    public void start() {
        threads = streams.collect { is ->
            Thread.start {
                def sb = new StringBuilder()
                byte[] buffer = new byte[BUFFER_SIZE]
                boolean exit = false

                while (true) {
                    while (is.available() > 0) {
                        int bytes = is.read(buffer, 0, BUFFER_SIZE)
                        if (bytes < 0) {
                            exit = true
                            break
                        }
                        sb.append(new String(buffer, 0, bytes))

                        int idx
                        while ((idx = sb.lastIndexOf('\n')) != -1) {
                            log.info sb.substring(0, idx).trim()
                            sb = sb.delete(0, idx + 1)
                        }
                    }
                    if (exit) {
                        break
                    }
                    sleep(1000L, { exit = true })
                }

                is.close()
            }
        }
    }

    /**
     * Interrupts all the stream loggers
     */
    public void interrupt() {
        threads*.interrupt()
    }

    /**
     * Joins to all the stream loggers
     */
    public void join() {
        threads*.join()
    }
}
