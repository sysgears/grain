package com.sysgears.grain.registry

import com.sysgears.grain.config.Config

import javax.inject.Inject

/**
 * Markup type detector for site files.
 */
@javax.inject.Singleton
public class MarkupDetector {
    
    /** Site config */
    @Inject private Config config 

    /**
     * Returns markup type for resource file
     * 
     * @param resource resource file
     * 
     * @return markup type
     */
    public String getMarkupType(final File resource) {
        def binaryFiles = config.binary_files ?: []

        if (binaryFiles.any { resource.canonicalPath.matches(it as String) }) {
            return 'binary'
        }
        
        def extension = resource.getExtension()

        def markup

        switch (extension) {
            case 'html':
            case 'htm':
                markup = 'html'
                break
            case 'md':
            case 'markdown':
                markup = 'md'
                break
            case 'rst':
                markup = 'rst'
                break
            case 'adoc':
            case 'asciidoctor':
                markup = 'adoc'
                break
            default:
                markup = 'text'
                break
        }
        
        markup
    }
}
