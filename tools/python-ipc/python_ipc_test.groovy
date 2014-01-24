import com.sysgears.grain.init.GrainSettings
import com.sysgears.grain.rpc.python.SetupToolsInstaller
import org.python.util.PythonInterpreter

import com.sysgears.grain.rpc.RPCDispatcher
import com.sysgears.grain.rpc.RPCExecutor
import com.sysgears.grain.rpc.TCPUtils

def useJython = true

def settings = new GrainSettings(grainHome: new File("${System.getProperty('user.home')}/.grain"),
                                 toolsHome: new File('.').canonicalFile) 

def installer = new SetupToolsInstaller()
installer.settings = settings 
installer.install()

def setupToolsDir = "${settings.grainHome}/packages/python/setuptools" 

def example = { RPCDispatcher rpc ->

    def html = rpc.with {
        ipc.add_lib_path("${setupToolsDir}/setuptools-2.1.egg")
        ipc.set_user_base("${System.getProperty('user.home')}/.grain/packages/python/")
        ipc.install_package('docutils')
        ipc.add_lib_path('../docutils-bridge')
        docutils_bridge.process('*my text, ляля*')
    }
    
    println html
}

def serverSocket = TCPUtils.firstAvailablePort

Thread err = null, out = null
if (useJython) {
    Thread.start {
        def code = """
import sys
sys.path.append('${settings.toolsHome}')
import ipc
ipc.main(${serverSocket.localPort})
"""

        System.setProperty('python.executable', 'ipc.py')
        python = new PythonInterpreter()
        python.exec(code)        
    }
} else {
    def proc = "/usr/bin/python ipc.py ${serverSocket.localPort}".execute([
            'PYTHONUSERBASE=/home/victor/.grain/packages/python/'
    ], new File('.'))
    err = proc.consumeProcessErrorStream(System.err)
    out = proc.consumeProcessOutputStream(System.out)
}

try {
    def socket = serverSocket.accept()
    def executor = new RPCExecutor(socket.inputStream, socket.outputStream)
    executor.start()
    def rpc = new RPCDispatcher(executor)

    example(rpc)
} finally {
    serverSocket.close()
}

if (!useJython) {
    out?.join()
    err?.join()
}

