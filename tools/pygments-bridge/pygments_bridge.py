import pygments
from pygments import lexers, formatters, styles, filters

def highlight(code, language):
    lexer = lexers.get_lexer_by_name(language)
    formatter = pygments.formatters.get_formatter_by_name('html', outencoding = 'utf-8')    
    res = pygments.highlight(code, lexer, formatter)
    return res
