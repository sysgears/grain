require 'asciidoctor'

module AsciidocBridge
  def convert(source, highlighter)
    Asciidoctor.render(source, :attributes => { "source-highlighter" => "coderay" })
  end

  module_function :convert
end