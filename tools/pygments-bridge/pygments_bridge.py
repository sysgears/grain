import pygments, sys
from pygments import lexers, formatters, styles, filters

def highlight(code, language):
    try:
        lexer = lexers.get_lexer_by_name(language)
        formatter = pygments.formatters.get_formatter_by_name('html', outencoding = 'utf-8')
        res = pygments.highlight(code, lexer, formatter)
        return res
    except:
        sys.stderr.write("Pygments error: an exception occurred while highlighting the code")
        sys.stderr.write("\nLanguage: %s"%((language)))
        sys.stderr.write("\nCode:\n%s\n"%((code)))
        sys.stderr.flush()
        raise
