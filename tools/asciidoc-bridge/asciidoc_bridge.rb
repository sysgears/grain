require 'asciidoctor'

module AsciidocBridge
  def convert(source, attributes)
    begin
      return Asciidoctor.convert(source, :safe => 0, :attributes => attributes)
    rescue Exception => e
      STDERR.puts e.inspect
      STDERR.puts e.backtrace
      return "<pre>" + ["#{e.backtrace.first}: #{e.message} (#{e.class})", e.backtrace.drop(1).map{|s| "\t#{s}"}].join("\n") + "</pre>"
    end
  end

  module_function :convert
end