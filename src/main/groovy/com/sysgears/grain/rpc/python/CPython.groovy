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
import com.sysgears.grain.service.ServiceManager
import groovy.util.logging.Slf4j

import javax.inject.Inject

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

    /** Setup tools installer */
    @Inject private SetupToolsInstaller installer

    /** Python RPC implementation */
    private RPCDispatcher rpc

    /** Process streams logger */
    private StreamLogger streamLogger

    /** IPC socket */
    private ServerSocket serverSocket

    /** Python process */
    private Process process

    /**
     * Starts CPython process.
     */
    public void start() {
        log.info 'Launching Python process...'

        serverSocket = TCPUtils.firstAvailablePort
        if (!serverSocket)
            throw new RuntimeException("Unable to allocate socket for IPC, all TCP ports are busy")
        
        try {
            serverSocket.setSoTimeout(30000)
            def port = serverSocket.getLocalPort()

            def setupToolsPath = installer.install()

            def pythonCmd = pythonFinder.cmd.command

            def cmdline = [pythonCmd, "${settings.toolsHome}/python-ipc/ipc.py", port]

            log.info cmdline.join(' ')

            process = cmdline.execute(["PYTHONUSERBASE=${settings.grainHome}/packages/python/"], new File("."))

            def socket = serverSocket.accept()

            def executor = executorFactory.create(socket.inputStream, socket.outputStream)
            executor.start()

            def rpc = dispatcherFactory.create(executor)
            streamLogger = streamLoggerFactory.create(process.in, process.err)
            streamLogger.start()
            
            rpc.ipc.add_lib_path(setupToolsPath)

            Thread.startDaemon {
                process.waitFor()
                streamLogger.interrupt()
            }

            this.rpc = rpc            
        } catch (e) {
            serverSocket.close()
            throw e
        }
    }

    /**
     * Shuts down CPython process 
     *
     * @throws Exception in case some error occur
     */
    @Override
    public void stop() {
        log.info 'Stopping Python process...'
        streamLogger.interrupt()
        streamLogger.join()
        process.destroy()
        serverSocket.close()
        log.info 'Python process finished...'
    }

    /**
     * @inheritDoc 
     */
    @Override
    public RPCDispatcher getRpc() {
        rpc
    }

    /**
     * @inheritDoc
     */
    @Override
    public void configChanged() {
    }
}
