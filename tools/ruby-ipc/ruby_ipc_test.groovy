import com.sysgears.grain.rpc.RPCDispatcher
import com.sysgears.grain.rpc.RPCExecutor
import com.sysgears.grain.rpc.TCPUtils

def example = { RPCDispatcher rpc ->
    rpc.Ipc.set_gem_home('/home/victor/.grain/gems')
    println rpc.Ipc.install_gem('asciidoctor')
    
    def html = rpc.with {
        Ipc.require('asciidoctor')
        Asciidoctor.render('*This* is it ляля.')
    }

    println html
}

def serverSocket = TCPUtils.firstAvailablePort

def proc = "${System.getProperty('user.home')}/.rvm/bin/ruby ipc.rb ${serverSocket.localPort}".execute()
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