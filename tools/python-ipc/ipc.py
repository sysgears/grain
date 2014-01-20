#!/usr/bin/env python

import os, sys, site, struct, socket, traceback, urllib2, errno, tempfile

def _read_integer(sf):
    return struct.unpack('>i', sf.read(4))[0]

def _read_string(sf):
    return sf.read(_read_integer(sf))

def _write_string(sf, result):
    if result == None:
        sf.write(struct.pack('>i', -1))
        sf.flush()
    else:
        ret = unicode(result).encode('utf-8')
        sf.write(struct.pack('>i', len(ret)))
        sf.write(ret)
        sf.flush()

def _mkdir_p(path):
    try:
        os.makedirs(path)
    except OSError as exc:
        if exc.errno == errno.EEXIST and os.path.isdir(path):
            pass
        else: raise

def add_lib_path(path):
    sys.path.insert(1, path)

def install_setup_tools():
    _mkdir_p(site.USER_BASE)
    
    if not os.path.isfile(site.USER_BASE + 'bin/easy_install'):
        ez = {}
        exec(urllib2.urlopen('https://bitbucket.org/pypa/setuptools/raw/bootstrap/ez_setup.py'
            ).read(), ez)
    
        sys.argv = [site.USER_BASE + 'ez_setup.py', '--user']
        ez['main']()
        site.addsitedir(site.USER_BASE)

def install_package(pkg_name):
    import setuptools, pkg_resources
    try:
        pkg_resources.require(pkg_name)
    except:
        from setuptools.command import easy_install
        easy_install.main(['--user', '-U', pkg_name])

        pkg_resources.get_distribution(pkg_name).activate()
    
def main():

    sys.stderr.write("Starting Python IPC on port: %s\n"%((sys.argv[1])))
    sys.stderr.flush()

    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.connect(("localhost", int(sys.argv[1])))
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
            result = func(*args)

            #sys.stderr.write("%s.%s(%s):\n%s\n"%((module, func, args, result)))
            #sys.stderr.flush()

            _write_string(sf, result)
        except IOError:
            raise
        except:
            traceback.print_exc(file=sys.stderr)

if __name__ == "__main__":
    main()
