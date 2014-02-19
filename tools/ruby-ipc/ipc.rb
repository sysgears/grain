require 'socket'

# Inter process communication protocol between Groovy and Ruby implementation   
class Ipc
  
  def initialize(socket)
    @socket = socket
  end

  # Checks if gem installed
  def self.gem_installed(name, version = Gem::Requirement.default)
    require 'rubygems'
    require 'rubygems/dependency_installer'
    version = Gem::Requirement.create version unless version.is_a? Gem::Requirement
    Gem::Specification.each.any? { |spec| name == spec.name and version.satisfied_by? spec.version }
  end

  # Installs gem and returns installed gem version
  def self.install_gem(name, version = Gem::Requirement.default)
    if gem_installed name, version
      return Gem.latest_spec_for(name).version.to_s
    end

    STDERR.puts "Downloading gem #{name} #{version}"
    
    Gem::DependencyInstaller.new.install name, version

    return Gem.latest_spec_for(name).version.to_s
  end

  # Adds gem path to the search path list for looking up gems
  def self.set_gem_home gempath
    require 'rubygems'
    require 'rubygems/dependency_installer'
    ENV['GEM_HOME']=gempath
    ENV['GEM_PATH']=ENV['GEM_PATH'] || gempath 
    Gem.clear_paths
  end
  
  # Adds library directory to load path
  def self.add_lib_path libpath
    $LOAD_PATH.unshift(libpath)
  end
  
  # Loops forever reading RPC requests, executing them and sending return value in response
  def run
    while true do
      begin
        class_name = read_string
        func_name = read_string
        args_count = read_integer
  
        args = []
  
        args_count.times do
          arg = read_string
          args <<= arg
        end

        clazz = Kernel.const_get(class_name)
        result = clazz.send(func_name, *args).to_s

        write_string(result)
      rescue SystemExit
        if RUBY_PLATFORM == "java"
          runnable = Class.new do
            def run
              java.lang.System.exit(0)
            end
          end
          thread = java.lang.Thread.new(runnable.new)
          thread.setDaemon(true)
          thread.start
        else
          raise
        end
      rescue Interrupt
        raise
      rescue IOError
        raise
      rescue Exception => e
        STDERR.puts e.inspect
        STDERR.puts e.backtrace
      end
    end
  end

  # Reads integer in network format from stdin
  def read_integer
    @socket.read(4).unpack("N")[0]
  end

  # Reads string in network format from stdin
  def read_string
    len = read_integer
    @socket.read(len).unpack("a*")[0]
  end

  # Writes string to stdout
  def write_string(result)
    @socket.write([result.bytesize].pack("N"))
    @socket.write(result)
    @socket.flush
  end

end

# Instantiate IPC implementation and launch processing loop
STDERR.puts "Starting Ruby IPC on port " + ARGV[0]
socket = TCPSocket.open('localhost', ARGV[0])
ipc = Ipc.new(socket)
ipc.run
