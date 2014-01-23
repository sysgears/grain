import docutils

from docutils.core import publish_parts

def process(source):
    parts = publish_parts(source = source, writer_name = 'html')
    
    return parts['html_body']
