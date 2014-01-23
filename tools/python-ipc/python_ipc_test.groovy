import org.apache.commons.io.FileUtils
import org.python.util.PythonInterpreter

import com.sysgears.grain.rpc.RPCDispatcher
import com.sysgears.grain.rpc.RPCExecutor
import com.sysgears.grain.rpc.TCPUtils

def useJython = false

def setupToolsDir = new File("${System.getProperty('user.home')}/.grain/packages/python/setuptools")
if (!setupToolsDir.exists()) {
    bootstrapSetupTools()
}

def example = { RPCDispatcher rpc ->

    def html = rpc.with {
        ipc.add_lib_path("${setupToolsDir.toString()}/setuptools-2.1.egg")
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
        def code = '''
from distutils.command.install import INSTALL_SCHEMES
INSTALL_SCHEMES['java_user'] = {
        'purelib': '$usersite',
        'platlib': '$usersite',
        'headers': '$userbase/include/python$py_version_short/$dist_name',
        'scripts': '$userbase/bin',
        'data'   : '$userbase',
        }
''' + """
import sys, ipc, urllib2
ipc.set_user_base('${System.getProperty('user.home')}/.grain/packages/python/')
ipc.add_lib_path("${setupToolsDir.toString()}/setuptools-2.1.egg")
if sys.platform.startswith('java'):
    try:
        import setuptools
        from setuptools import ssl_support
        ssl_support.is_available = False
        print "SSL support: %s" % (ssl_support.is_available)
    except:
        pass
    
from java.net import URL
from java.io import BufferedInputStream
from jarray import zeros

org_urlopen = urllib2.urlopen 

class Headers(dict):
    def getheaders(self, key):
        value = self.get(key.lower())
        if (value == None):
            return list()
        else:
            return list(value)
    
class ConnReader:        
    def __init__(self, conn):
        self.conn = conn
        self.url = conn.getURL().toExternalForm()
        
        fields = self.conn.getHeaderFields()
        self.headers = Headers()
        for key in fields:
            if key != None:
                self.headers[key.lower()] = conn.getHeaderField(key)
        self.bs = BufferedInputStream(self.conn.getInputStream()) 
                
    def read(self, *args):
        if len(args) == 1:
            size = args[0]
            buf = zeros(size, 'b')
            off = 0
            while size > 0:
                count = self.bs.read(buf, off, size)
                if count == -1:
                    buf = buf[:off]
                    break
                off += count
                size -= count
                
            return buf.tostring()
        else:
            return self.read(int(self.headers['content-length']))
    
    def info(self):        
        return Headers(self.headers)
        
    def close(self):
        self.bs.close()
     
def new_urlopen(req):
    if isinstance(req, str):
        full_url = req
    else:
        full_url = req.get_full_url()
    if full_url.startswith('https'): 
        u = URL(full_url)
        conn = u.openConnection()
        return ConnReader(conn)
    else:
        org_urlopen(req)

urllib2.urlopen = new_urlopen

#result = urllib2.urlopen("https://pypi.python.org/simple/docutils/").read()
#print result

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

def bootstrapSetupTools() {
    def tempDir = File.createTempDir()
    try {
        def tarballName = 'setuptools-2.1.tar.gz'
        def baseUrl = 'https://pypi.python.org/packages/source/s/setuptools/'
        def tarball = new File(tempDir, tarballName)
        def ant = new AntBuilder()
        tarball << new URL(baseUrl + tarballName).openStream()

        def setupDir = new File(tempDir, 'setuptools')
        setupDir.mkdir()
        new File(setupDir, 'setuptools.pth') << "./setuptools-2.1.egg\n"
        def eggDir = new File(setupDir, 'setuptools-2.1.egg')
        eggDir.mkdir()

        def sourceDir = new File(tempDir, 'setuptools-2.1')
        def packageLib = "${System.getProperty('user.home')}/.grain/packages/python/lib/jython2.7/site-packages" 

        ant.sequential() {
            untar(src: tarball, dest: tempDir, compression: 'gzip')
            copy(todir:"${eggDir}/EGG-INFO") {
                fileset(dir: "${sourceDir}/setuptools.egg-info")
            }
            copy(todir:"${eggDir}/_markerlib") {
                fileset(dir: "${sourceDir}/_markerlib")
            }
            copy(todir:"${eggDir}/setuptools") {
                fileset(dir: "${sourceDir}/setuptools") {
                    include(name:"**/*.py")
                }
            }
            copy(todir:"${eggDir}") {
                fileset(dir: sourceDir) {
                    include(name: "easy_install.py")
                    include(name: "pkg_resources.py")
                }
            }
            move(todir:"${System.getProperty('user.home')}/.grain/packages/python/setuptools") {
                fileset(dir: setupDir)
            }
        }
    } finally {
        FileUtils.deleteDirectory(tempDir)
    }
}
