import com.sysgears.grain.rpc.RPCDispatcher
import com.sysgears.grain.rpc.RPCExecutor
import com.sysgears.grain.rpc.TCPUtils

def example = { RPCDispatcher rpc ->

    def html = rpc.with {
        ipc.install_setup_tools()
        ipc.install_package('docutils')
        ipc.add_lib_path('../docutils-bridge')
        docutils_bridge.process('*my text, ляля*')
    }
    
    println html
}

def serverSocket = TCPUtils.firstAvailablePort

def proc = "/usr/bin/python ipc.py ${serverSocket.localPort}".execute(['PYTHONUSERBASE=/home/victor/.grain/packages/python/'], new File('.'))
def err = proc.consumeProcessErrorStream(System.err)
def out = proc.consumeProcessOutputStream(System.out)

try {
    def socket = serverSocket.accept()
    def executor = new RPCExecutor(socket.inputStream, socket.outputStream)
    executor.start()
    def rpc = new RPCDispatcher(executor)

    example(rpc)
} finally {
    serverSocket.close()
}

out.join()
err.join()
