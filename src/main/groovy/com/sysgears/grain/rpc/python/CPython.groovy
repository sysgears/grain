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

import com.sysgears.grain.init.GrainSettings
import com.sysgears.grain.log.StreamLogger
import com.sysgears.grain.log.StreamLoggerFactory
import com.sysgears.grain.rpc.RPCDispatcher
import com.sysgears.grain.rpc.RPCDispatcherFactory
import com.sysgears.grain.rpc.RPCExecutorFactory
import com.sysgears.grain.rpc.TCPUtils
import groovy.util.logging.Slf4j

import javax.inject.Inject
import java.util.concurrent.CountDownLatch

/**
 * CPython process launcher.
 */
@javax.inject.Singleton
@Slf4j
public class CPython implements Python {

    /** Stream logger factory */
    @Inject private StreamLoggerFactory streamLoggerFactory

    /** Grain settings */
    @Inject private GrainSettings settings

    /** Python system command finder */
    @Inject private PythonFinder pythonFinder

    /** RPC executor factory */
    @Inject private RPCExecutorFactory executorFactory

    /** RPC dispatcher factory */
    @Inject private RPCDispatcherFactory dispatcherFactory

    /** Python RPC implementation */
    private RPCDispatcher rpc

    /** CPython thread */
    private Thread thread

    /** Process streams logger */
    private StreamLogger streamLogger

    /** Mutex for pygments starting and using */
    private CountDownLatch latch

    /** Memorize Python command, to restart service when python command changes */
    private String pythonCmd

    /**
     * Starts CPython process.
     */
    public void start() {
        latch = new CountDownLatch(1)
        thread = Thread.start {
            def serverSocket = null
            try {
                log.info 'Launching Python process...'

                pythonCmd = pythonFinder.cmd

                serverSocket = TCPUtils.firstAvailablePort
                if (!serverSocket)
                    throw new RuntimeException("Unable to allocate socket for IPC, all TCP ports are busy")
                def port = serverSocket.getLocalPort()

                def cmdline = [pythonCmd, "${settings.toolsHome}/python-ipc/ipc.py", port]

                log.info cmdline.join(' ')

                def process = cmdline.execute()

                def socket = serverSocket.accept()

                def executor = executorFactory.create(socket.inputStream, socket.outputStream)
                executor.start()

                rpc = dispatcherFactory.create(executor)
                streamLogger = streamLoggerFactory.create(process.in, process.err)
                streamLogger.start()
                latch.countDown()
                def watcher = Thread.start {
                    process.waitFor()
                    streamLogger.interrupt()
                }
                streamLogger.join()
                process.destroy()
                watcher.join()
                log.info 'Python process finished...'
            } catch (t) {
                log.error("Error running Python", t)
                latch.countDown()
            } finally {
                if (serverSocket != null)
                    serverSocket.close()
            }
        }
    }

    /**
     * Shuts down CPython process 
     *
     * @throws Exception in case some error occur
     */
    @Override
    public void stop() {
        if (latch) {
            log.info 'Stopping Python process...'
            latch.await()
            streamLogger?.interrupt()
            thread.join()
            latch = null
        }
    }

    /**
     * @inheritDoc 
     */
    @Override
    public RPCDispatcher getRpc() {
        if (!latch) {
            start()
        }
        latch.await()
        rpc
    }

    /**
     * Restart Python when python command changes
     */
    @Override
    public void configChanged() {
        if (latch) {
            latch.await()
            if (pythonCmd != pythonFinder.cmd) {
                stop()
                start()
            }
        }
    }
}
