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
 * RMI Ruby process launcher.
 */
@javax.inject.Singleton
@Slf4j
public class RMIRuby implements Ruby {

    /** Grain settings */
    @Inject private GrainSettings settings

    /** Stream logger factory */
    @Inject private StreamLoggerFactory streamLoggerFactory

    /** Ruby system command finder */
    @Inject private RubyFinder rubyFinder

    /** RPC executor factory */
    @Inject private RPCExecutorFactory executorFactory

    /** RPC dispatcher factory */
    @Inject private RPCDispatcherFactory dispatcherFactory

    /** Memorize Ruby command, to restart service when ruby command changes */
    private String rubyCmd

    /** Ruby RPC implementation */
    private RPCDispatcher rpc

    /** Process streams logger */
    private StreamLogger streamLogger

    /** Mutex for RMI Ruby starting */
    private CountDownLatch latch

    /** RMI Ruby process thread */
    private Thread thread

    /**
     * Starts RMI Ruby process
     */
    public void start() {
        latch = new CountDownLatch(1)
        thread = Thread.start {
            def serverSocket = null
            try {
                log.info "Launching RMI Ruby process..."

                def rubyGemsDir = installer.install()

                rubyCmd = rubyFinder.cmd

                serverSocket = TCPUtils.firstAvailablePort
                if (!serverSocket)
                    throw new RuntimeException("Unable to allocate socket for IPC, all TCP ports are busy")
                serverSocket.setSoTimeout(5000)
                def port = serverSocket.getLocalPort()
                
                def cmdline = [rubyCmd, "${settings.toolsHome}/ruby-ipc/ipc.rb", port]

                log.info cmdline.join(' ')

                def process = cmdline.execute()

                def socket = serverSocket.accept()

                def executor = executorFactory.create(socket.inputStream, socket.outputStream) 
                executor.start()
                
                def rpc = dispatcherFactory.create(executor)

                streamLogger = streamLoggerFactory.create(process.in, process.err)
                streamLogger.start()

                rpc.Ipc.add_lib_path(rubyGemsDir)
                rpc.Ipc.set_gem_home("${settings.grainHome}/packages/ruby")

                this.rpc = rpc
                latch.countDown()

                def watcher = Thread.start {
                    process.waitFor()
                    streamLogger.interrupt()
                }
                streamLogger.join()
                process.destroy()
                watcher.join()
                log.info 'RMI Ruby process finished...'
            } catch (t) {
                log.error("Error running RMI Ruby", t)
                latch.countDown()
            } finally {
                if (serverSocket != null)
                    serverSocket.close()
            }
        }
    }

    /**
     * Shuts down RMI Ruby process 
     *
     * @throws Exception in case some error occur
     */
    @Override
    public void stop() {
        if (latch) {
            log.info 'Stopping RMI Ruby process...'
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
     * Restart Ruby when ruby command changes
     */
    @Override
    public void configChanged() {
        if (latch) {
            latch.await()
            if (rubyCmd != rubyFinder.cmd) {
                stop()
                start()
            }
        }
    }
}
