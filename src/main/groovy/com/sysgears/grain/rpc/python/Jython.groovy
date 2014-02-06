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
import java.util.concurrent.CountDownLatch

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

    /** Mutex for Jython starting */
    private CountDownLatch latch

    /** Jython thread */
    private Thread thread
    
    /**
     * Starts Jython process 
     */
    public void start() {
        latch = new CountDownLatch(1)
        thread = Thread.start {
            ServerSocket serverSocket = null
            try {
                log.info "Launching Jython process..."
                
                def setupToolsPath = installer.install()

                serverSocket = TCPUtils.firstAvailablePort
                if (!serverSocket)
                    throw new RuntimeException("Unable to allocate socket for IPC, all TCP ports are busy")
                serverSocket.setSoTimeout(30000)
                def port = serverSocket.getLocalPort()
                def ipcPath = new File("${settings.toolsHome}/python-ipc/ipc.py").canonicalPath 

                def args = [ipcPath, port] as String[]

                log.info args.join(' ')

                System.setProperty('python.executable', ipcPath)

                python = new PythonInterpreter()
                python.setIn(new ByteArrayInputStream())
                python.setOut(new LoggingOutputStream())
                python.setErr(new LoggingOutputStream())
                
                Thread.startDaemon {
                    try {
                        def socket = serverSocket.accept()

                        def executor = executorFactory.create(socket.inputStream, socket.outputStream)
                        executor.start()

                        rpc = dispatcherFactory.create(executor)
                    } finally {
                        latch.countDown()
                    }
                }

                try {
                    python.exec("""
import os, sys
sys.path.append('${new File(settings.toolsHome, 'python-ipc').canonicalPath}') 
import ipc
ipc.set_user_base('${new File(settings.grainHome, 'packages/python').canonicalPath}')
ipc.add_lib_path("${setupToolsPath}")
ipc.main($port)""")
                } catch (PyException pe) {
                    pe.printStackTrace()
                    log.error("Error while running Jython", pe)
                } catch (t) {
                    log.error("Error while running Jython", t)
                } finally {
                    if (serverSocket != null)
                        serverSocket.close()
                }
                log.info 'Jython process finished...'
            } catch (t) {
                log.error("Error launching Jython", t)
            } finally {
                latch.countDown()
            }
        }
    }

    /**
     * Shuts down JRuby process  
     *
     * @throws Exception in case some error occur
     */
    @Override
    public void stop() {
        if (latch) {
            log.info 'Stopping Jython process...'
            latch.await()
            python?.cleanup()
            python = null
            latch = null
        }
    }

    @Override
    public RPCDispatcher getRpc() {
        if (!latch) {
            start()
        }
        latch.await()
        rpc
    }

    @Override
    void configChanged() {
    }
    
}
