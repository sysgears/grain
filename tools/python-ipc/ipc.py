#!/usr/bin/env python

import os, sys, struct, socket, traceback, urllib2, errno, tempfile

from distutils import log

# Parameter data types that are supported for deconversion by the IPC
PARAM_DATA_TYPES = {'string' : 0, 'dictionary' : 1}

def _read_integer(sf):
    return struct.unpack('>i', sf.read(4))[0]

def _read_string(sf):
    return sf.read(_read_integer(sf))

def _read_dictionary(sf):
    dictionary = {}
    size = _read_integer(sf)
    for _ in xrange(size):
        key = _read_string(sf)
        value = _read_string(sf)
        dictionary[key] = value
    return dictionary

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
    except OSError as exc:
        if exc.errno in [0, 20000, 20047, errno.EEXIST] and os.path.isdir(path):
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

def set_user_site(user_site):
    import site
    site.USER_SITE = user_site

# Installs python package from PyPI and returns package version
def install_package(pkg_name):
    import setuptools, pkg_resources, site, sysconfig
    from setuptools.command import easy_install

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

        old_current_umask = easy_install.current_umask
        import subprocess
        def new_current_umask():
            umask = int(subprocess.check_output("umask", shell=True))
            return umask
        easy_install.current_umask = new_current_umask

        old_rmtree = easy_install.rmtree
        def new_rmtree(path, ignore_errors=False, onerror=easy_install.auto_chmod):
            try:
                old_rmtree(path, ignore_errors, onerror)
            except:
                sys.stderr.write("Unable to remove temporary directory : %s\n"%((path)))
                sys.stderr.flush()
        easy_install.rmtree = new_rmtree
    try:
        dist = pkg_resources.get_distribution(pkg_name)
    except:
        log.warn("Downloading package %s...", pkg_name)
        mkdir_p(site.USER_SITE)
        easy_install.main(argv = ['--user', '-U', pkg_name])

        site.addsitedir(sysconfig.get_path('platlib', os.name + '_user'))
        reload(pkg_resources)

        dist = pkg_resources.get_distribution(pkg_name)
        dist.activate()
    if sys.platform.startswith('java'):
        urllib2.urlopen = org_urlopen
        easy_install.rmtree = old_rmtree
        easy_install.current_umask = old_current_umask
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
                type = _read_integer(sf)
                if type == PARAM_DATA_TYPES['string']:
                    args.append(_read_string(sf))
                elif type == PARAM_DATA_TYPES['dictionary']:
                    args.append(_read_dictionary(sf))
                else:
                    raise ValueError("Can't recognize parameter data type with id %s" % type)

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
            return
        except IOError:
            raise
        except:
            traceback.print_exc(file=sys.stderr)
            sys.stderr.flush()

if __name__ == "__main__":
    main(int(sys.argv[1]))
