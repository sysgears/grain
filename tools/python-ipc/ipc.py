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

def mkdir_p(path):
    try:
        os.makedirs(path)
    except OSError, exc:
        if exc.errno == errno.EEXIST and os.path.isdir(path):
            pass
        else: raise

def add_lib_path(path):
    sys.path.insert(1, path)
    
def set_user_base(user_base):
    os.environ['PYTHONUSERBASE'] = user_base
    import site
    site.USER_BASE = user_base
    
def install_package(pkg_name):
    import setuptools, pkg_resources, site, sysconfig
    from setuptools.command import easy_install
    try:
        pkg_resources.require(pkg_name)
    except:
        easy_install.main(argv = ['-v', '--user', '-U', pkg_name])
        
        site.addsitedir(sysconfig.get_path('platlib', os.name + '_user'))
        reload(pkg_resources)
        
        pkg_resources.get_distribution(pkg_name).activate()
    
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
        except IOError:
            raise
        except:
            traceback.print_exc(file=sys.stderr)
            sys.stderr.flush()

if __name__ == "__main__":
    main(int(sys.argv[1]))
