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
import com.sysgears.grain.log.LoggingOutputStream
import com.sysgears.grain.rpc.RPCDispatcher
import com.sysgears.grain.rpc.RPCDispatcherFactory
import com.sysgears.grain.rpc.RPCExecutorFactory
import com.sysgears.grain.rpc.TCPUtils
import groovy.util.logging.Slf4j
import org.python.core.PyException
import org.python.util.PythonInterpreter

import javax.inject.Inject

/**
 * Jython launcher.
 */
@javax.inject.Singleton
@Slf4j
public class Jython implements Python {

    /** Grain settings */
    @Inject private GrainSettings settings

    /** RPC executor factory */
    @Inject private RPCExecutorFactory executorFactory

    /** RPC dispatcher factory */
    @Inject private RPCDispatcherFactory dispatcherFactory
    
    /** Setup tools installer */
    @Inject private SetupToolsInstaller installer

    /** Jython interpreter instance */
    private PythonInterpreter python

    /** Jython RPC implementation */
    private RPCDispatcher rpc

    /** IPC socket */
    private ServerSocket serverSocket

    /** Jython thread */
    private Thread thread
    
    /**
     * Starts Jython process 
     */
    public void start() {
        log.info "Launching Jython process..."

        serverSocket = TCPUtils.firstAvailablePort
        if (!serverSocket)
            throw new RuntimeException("Unable to allocate socket for IPC, all TCP ports are busy")

        try {
            serverSocket.setSoTimeout(30000)
            def port = serverSocket.getLocalPort()

            def setupToolsPath = installer.install()

            def ipcPath = new File("${settings.toolsHome}/python-ipc/ipc.py").canonicalPath 

            def args = [ipcPath, port] as String[]

            log.info args.join(' ')

            System.setProperty('python.executable', ipcPath)

            thread = Thread.startDaemon {
                try {
                    python = new PythonInterpreter()
                    python.setIn(new ByteArrayInputStream())
                    python.setOut(new LoggingOutputStream())
                    python.setErr(new LoggingOutputStream())

                    python.exec("""
import os, sys
sys.path.append('${new File(settings.toolsHome, 'python-ipc').canonicalPath}') 
import ipc
ipc.set_user_base('${new File(settings.grainHome, 'packages/python').canonicalPath}')
ipc.add_lib_path("${setupToolsPath}")
ipc.main($port)""")
                } catch (PyException pe) {
                    if (!pe.value.toString().contains('ClosedByInterruptException')) {
                        log.error("Error while running Jython:\n${pe.traceback.dumpStack()}")
                    }
                } catch (t) {
                    log.error("Error while running Jython", t)
                } 
                log.info 'Jython process finished...'
            }

            def socket = serverSocket.accept()

            def executor = executorFactory.create(socket.inputStream, socket.outputStream)
            executor.start()

            rpc = dispatcherFactory.create(executor)
        } catch (e) {
            serverSocket.close()
            throw e
        }
    }

    /**
     * Shuts down JRuby process  
     *
     * @throws Exception in case some error occur
     */
    @Override
    public void stop() {
        log.info 'Stopping Jython process...'
        thread.interrupt()
        thread.join()
        serverSocket.close()
        python.cleanup()
    }

    @Override
    public RPCDispatcher getRpc() {
        rpc
    }

    @Override
    void configChanged() {
    }
    
}
