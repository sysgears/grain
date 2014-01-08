package com.sysgears.grain.rst

import groovy.util.logging.Slf4j
import org.dom4j.io.OutputFormat
import org.dom4j.io.XMLWriter
import org.nuiton.jrst.JRSTGenerator
import org.nuiton.jrst.legacy.JRSTReader
import org.nuiton.util.Resource

import javax.inject.Named

/**
 * reStructuredText processor which uses JRst implementation.
 */
@javax.inject.Singleton
@Slf4j
class JRstProcessor implements RstProcessor {

    /**
     * Process source reStructuredText and returns the result 
     *
     * @param source reStructuredText
     *
     * @return output
     */
    public String process(String source) {
        def reader = new JRSTReader()
        def doc = reader.read(new StringReader(source))
        def gen = new JRSTGenerator()
        def xsl = Resource.getURL('/xsl/rst2xhtml.xsl')
        doc = gen.transform(doc, xsl)
        def output = new StringWriter()
        def writer = new XMLWriter(output, new OutputFormat("  ", true, "UTF-8"))
        try {
            writer.write(doc)
        } finally {
            writer.close()
        }
        output.toString()
    }

    /**
     * @inheritDoc
     */
    @Override
    public void configChanged() {
    }
}
