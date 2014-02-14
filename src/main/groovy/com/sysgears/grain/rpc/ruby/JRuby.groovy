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
import com.sysgears.grain.log.LoggingOutputStream
import com.sysgears.grain.rpc.RPCDispatcher
import com.sysgears.grain.rpc.RPCDispatcherFactory
import com.sysgears.grain.rpc.RPCExecutorFactory
import com.sysgears.grain.rpc.TCPUtils
import groovy.util.logging.Slf4j
import org.jruby.Ruby
import org.jruby.RubyInstanceConfig
import org.jruby.exceptions.RaiseException

import javax.inject.Inject
import java.util.concurrent.CountDownLatch

/**
 * JRuby launcher.
 */
@javax.inject.Singleton
@Slf4j
public class JRuby implements com.sysgears.grain.rpc.ruby.Ruby {

    /** Grain settings */
    @Inject private GrainSettings settings

    /** RPC executor factory */
    @Inject private RPCExecutorFactory executorFactory

    /** RPC dispatcher factory */
    @Inject private RPCDispatcherFactory dispatcherFactory

    /** JRuby interpreter instance */
    private Ruby ruby

    /** JRuby RPC implementation */
    private RPCDispatcher rpc

    /** JRuby thread */
    private Thread thread
    
    /** Whether stop is in process */
    private volatile stopInProcess = false

    /**
     * Starts JRuby process 
     */
    public void start() {
        log.info "Launching JRuby process..."

        ServerSocket serverSocket = null
        try {
            serverSocket = TCPUtils.firstAvailablePort
            if (!serverSocket)
                throw new RuntimeException("Unable to allocate socket for IPC, all TCP ports are busy")
            serverSocket.setSoTimeout(30000)
            def port = serverSocket.getLocalPort()

            System.setProperty('jruby.gem.home', new File("${settings.grainHome}/packages/ruby").canonicalPath)
            System.setProperty('jruby.bindir', new File("${settings.grainHome}/packages/ruby/bin").canonicalPath)
            System.setProperty('jruby.compile.fastest', 'true')

            def config = new RubyInstanceConfig()

            def args = [new File("${settings.toolsHome}/ruby-ipc/ipc.rb").canonicalPath, port] as String[]

            log.info args.join(' ')

            config.processArguments(args)

            config.setOutput(new PrintStream(new LoggingOutputStream()))
            config.setError(new PrintStream(new LoggingOutputStream()))

            thread = Thread.startDaemon {
                try {
                    ruby = Ruby.newInstance(config)

                    def inp = config.getScriptSource();
                    config.processArguments(config.parseShebangOptions(inp));
                    def filename = config.displayedFileName();

                    try {
                        ruby.runFromMain(inp, filename)
                    } catch (RaiseException re) {
                        if (re.exception.toString() != "exit" && re.exception.toString() != "Interrupt") {
                            log.error("Error while running JRuby", re)
                        } else if (re.exception.toString() == "exit" && !stopInProcess) {
                            // User pressed CTRL-C which was intercepted by JRuby, terminating Grain
                            System.exit(0)
                        }
                    } catch (t) {
                        log.error("Error while running JRuby", t)
                    } finally {
                        if (serverSocket != null && !serverSocket.isClosed())
                            serverSocket.close()
                    }
                    log.info 'JRuby process finished...'
                } catch (t) {
                    log.error("Error launching JRuby", t)
                }
            }

            def socket = serverSocket.accept()

            def executor = executorFactory.create(socket.inputStream, socket.outputStream)
            executor.start()

            def rpc = dispatcherFactory.create(executor)

            rpc.Ipc.set_gem_home(new File("${settings.grainHome}/packages/ruby").canonicalPath)

            this.rpc = rpc
        } catch (t) {
            if (serverSocket != null && !serverSocket.isClosed())
                serverSocket.close()
            throw t
        }
    }

    /**
     * Shuts down JRuby process  
     *
     * @throws Exception in case some error occur
     */
    @Override
    public void stop() {
        log.info 'Stopping JRuby process...'
        stopInProcess = true
        ruby?.threadService?.mainThread?.internalRaise(ruby?.interrupt)
        thread.join()
        stopInProcess = false
    }

    @Override
    public RPCDispatcher getRpc() {
        rpc
    }

    @Override
    void configChanged() {
    }

}    
