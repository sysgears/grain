require 'compass'
require 'compass/exec'

module CompassBridge
  def start(*args)
    @@thread = Thread.new do
      runner = Proc.new do
        Compass::Exec::SubCommandUI.new(args).run!
      end
      runner.call
    end
  end
  
  def await
    @@thread.join
  end
  
  def stop
    @@thread.kill
    await
  end

  module_function :start, :stop, :await
end