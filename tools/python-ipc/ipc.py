#!/usr/bin/env python

import os, sys, struct, socket, traceback, urllib2, errno, tempfile

from distutils import log

def _read_integer(sf):
    return struct.unpack('>i', sf.read(4))[0]

def _read_string(sf):
    return sf.read(_read_integer(sf))

def _write_string(sf, result):
    if result == None:
        sf.write(struct.pack('>i', -1))
        sf.flush()
    else:
        try:
            result.decode('utf-8')
            ret = result
        except UnicodeError:
            ret = result.encode('utf-8')
        sf.write(struct.pack('>i', len(ret)))
        sf.write(ret)
        sf.flush()

# Creates dir with parent dirs creation as needed
def mkdir_p(path):
    try:
        os.makedirs(path)
    except OSError, exc:
        if exc.errno == errno.EEXIST and os.path.isdir(path):
            pass
        else: raise

# Adds path to Python's sys.path
def add_lib_path(path):
    sys.path.insert(1, path)
    
# Sets PYTHONUSERBASE for Jython
def set_user_base(user_base):
    os.environ['PYTHONUSERBASE'] = user_base
    import site
    site.USER_BASE = user_base

# Installs python package from PyPI and returns package version
def install_package(pkg_name):
    if sys.platform.startswith('java'):
        from distutils.command.install import INSTALL_SCHEMES
        INSTALL_SCHEMES['java_user'] = {
            'purelib': '$usersite',
            'platlib': '$usersite',
            'headers': '$userbase/include/python$py_version_short/$dist_name',
            'scripts': '$userbase/bin',
            'data'   : '$userbase',
        }
        import setuptools
        from setuptools import ssl_support
        ssl_support.is_available = False

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

    import setuptools, pkg_resources, site, sysconfig
    from setuptools.command import easy_install
    try:
        dist = pkg_resources.get_distribution(pkg_name)
    except:
        log.warn("Downloading package %s...", pkg_name)
        easy_install.main(argv = ['--user', '-U', pkg_name])
        
        site.addsitedir(sysconfig.get_path('platlib', os.name + '_user'))
        reload(pkg_resources)
        
        dist = pkg_resources.get_distribution(pkg_name)
        dist.activate()
    if sys.platform.startswith('java'):
        urllib2.urlopen = org_urlopen
    return dist.version

def main(port):

    sys.stderr.write("Starting Python IPC on port: %s\n"%((port)))
    sys.stderr.flush()

    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.connect(("localhost", port))
    sf = s.makefile()

    while True:
        try:
            module_name = _read_string(sf)
            func_name = _read_string(sf)
            args_count = _read_integer(sf)

            args = []
            for _ in xrange(args_count):
                arg = _read_string(sf)
                args.append(arg)

            module =  __import__ (module_name)
            func = getattr(module, func_name)
            result = None
            try:
                result = func(*args)
            except:
                traceback.print_exc(file=sys.stderr)
                sys.stderr.flush()

            #sys.stderr.write("%s.%s(%s):\n%s\n"%((module, func, args, unicode(result).encode('utf-8'))))
            #sys.stderr.flush()

            _write_string(sf, result)
        except KeyboardInterrupt:
            raise 
        except IOError:
            raise
        except:
            traceback.print_exc(file=sys.stderr)
            sys.stderr.flush()

if __name__ == "__main__":
    main(int(sys.argv[1]))
