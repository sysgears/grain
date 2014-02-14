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

    /** RubyGems installer */
    @Inject private RubyGemsInstaller installer

    /** Ruby RPC implementation */
    private RPCDispatcher rpc

    /** Process streams logger */
    private StreamLogger streamLogger

    /** IPC socket */
    private ServerSocket serverSocket
    
    /** RMI Ruby process */
    private Process process

    /**
     * Starts RMI Ruby process
     */
    public void start() {
        log.info "Launching RMI Ruby process..."

        serverSocket = TCPUtils.firstAvailablePort
        if (!serverSocket)
            throw new RuntimeException("Unable to allocate socket for IPC, all TCP ports are busy")

        try {
            serverSocket.setSoTimeout(30000)
            def port = serverSocket.getLocalPort()
    
            def rubyGemsDir
            
            def rubyCmd = rubyFinder.cmd.command
            def ver = rubyFinder.cmd.version
            if (ver.startsWith('ruby 1.')) {
                // 1.8.11 version is compatible with Ubuntu 12.04 LTS Ruby 1.9.3p0
                rubyGemsDir = installer.install('1.8.11')
            } else {
                rubyGemsDir = installer.install()
            }
    
            def cmdline = [rubyCmd, "${settings.toolsHome}/ruby-ipc/ipc.rb", port]
    
            log.info cmdline.join(' ')
    
            process = cmdline.execute()
    
            def socket = serverSocket.accept()
    
            def executor = executorFactory.create(socket.inputStream, socket.outputStream) 
            executor.start()
            
            def rpc = dispatcherFactory.create(executor)
    
            streamLogger = streamLoggerFactory.create(process.in, process.err)
            streamLogger.start()

            Thread.startDaemon {
                process.waitFor()
                streamLogger.interrupt()
            }

            rpc.Ipc.add_lib_path(rubyGemsDir)
            rpc.Ipc.set_gem_home("${settings.grainHome}/packages/ruby")
    
            this.rpc = rpc    
        } catch (e) {
            serverSocket.close()
            throw e
        }
    }

    /**
     * Shuts down RMI Ruby process 
     *
     * @throws Exception in case some error occur
     */
    @Override
    public void stop() {
        log.info 'Stopping RMI Ruby process...'
        streamLogger.interrupt()
        streamLogger.join()
        process.destroy()
        serverSocket.close()
        log.info 'RMI Ruby process finished...'
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
