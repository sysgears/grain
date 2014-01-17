import com.sysgears.grain.rpc.RPCDispatcher
import com.sysgears.grain.rpc.RPCExecutor
import com.sysgears.grain.rpc.TCPUtils

def example = { RPCDispatcher rpc ->
    def html = rpc.with {
        ipc.add_lib_path('../pygments-bridge')
        pygments_bridge.highlight('int a = 10;', 'java')
    }

    println html
} 

def serverSocket = TCPUtils.firstAvailablePort

def proc = "python ipc.py ${serverSocket.localPort}".execute()
def thread = proc.consumeProcessErrorStream(System.err)

try {
    def socket = serverSocket.accept()
    def executor = new RPCExecutor(socket.inputStream, socket.outputStream)
    executor.start()
    def rpc = new RPCDispatcher(executor)

    example(rpc)
} finally {
    serverSocket.close()
}

thread.join()
