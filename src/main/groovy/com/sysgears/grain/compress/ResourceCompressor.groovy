/*
 * Copyright (c) 2013 SysGears, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sysgears.grain.compress

import com.googlecode.htmlcompressor.compressor.Compressor
import com.googlecode.htmlcompressor.compressor.HtmlCompressor
import com.googlecode.htmlcompressor.compressor.XmlCompressor
import com.googlecode.htmlcompressor.compressor.YuiCssCompressor
import com.googlecode.htmlcompressor.compressor.YuiJavaScriptCompressor
import com.sysgears.grain.taglib.Site

import javax.inject.Inject
import javax.inject.Named
import java.util.zip.GZIPOutputStream

/**
 * Resource compressor. Compresses resources in accordance with compression techniques
 * specified in site config.
 */
@javax.inject.Singleton
class ResourceCompressor {
    
    /** Site instance */
    @Inject private Site site

    /**
     * Determines primary resource compressor based on extension
     * 
     * @param extension resource file extension
     * 
     * @return compressor
     */
    private Compressor getFirstLevelCompressor(String extension) {
        Compressor compressor = null
        switch (extension) {
            case { it == 'js' && site.features?.minify_js }:
                compressor = new YuiJavaScriptCompressor()
                break
            case { it == 'css' && site.features?.minify_css }:
                compressor = new YuiCssCompressor()
                break
            case { it == 'xml' && site.features?.minify_xml }:
                compressor = new XmlCompressor()
                break
            case { it == 'html' && site.features?.minify_html }:
                compressor = new HtmlCompressor()
                break
        }
        compressor
    }

    /**
     * Returns whether GZIP compression enabled
     * 
     * @return whether GZIP compression enabled
     */
    public boolean isGzipEnabled() {
        site.features?.compress == 'gzip'
    }


    /**
     * Compresses a site resource (CSS, HTML, JS, etc)
     *
     * @param location resource location  
     * @param content rendered content of resource
     * 
     * @return compressed content of resource
     */
    byte[] compress(String location, byte[] content) {
        String extension = new File(location).getExtension()
        Compressor compressor = getFirstLevelCompressor(extension)
        def result = content
        if (compressor != null) {
            try {
                result = compressor.compress(new String(content)).bytes
            } catch (Throwable t) {
                throw new CompressException("Failed to compress the resource at: ${location}\n${new String(content)}", t)
            }
        }
        
        if (gzipEnabled) {
            // GZIP result 
            def targetStream = new ByteArrayOutputStream()
            def zipStream = new GZIPOutputStream(targetStream)
            zipStream.write(result)
            zipStream.close()
            def zipped = targetStream.toByteArray()
            targetStream.close()

            zipped
        } else {
            result
        }
    }
}
