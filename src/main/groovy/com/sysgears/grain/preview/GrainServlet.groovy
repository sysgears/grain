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

package com.sysgears.grain.preview

import com.sysgears.grain.compress.ResourceCompressor
import com.sysgears.grain.registry.URLRegistry
import com.sysgears.grain.exceptions.DefaultErrorsPrinter
import com.sysgears.grain.render.RenderException

import javax.servlet.ServletConfig
import javax.servlet.ServletContext
import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Grain servlet - serves resources in accordance with URL to resource mapping. 
 */
class GrainServlet extends HttpServlet {
    
    /** Grain URL registry */
    private URLRegistry urlRegistry
    
    /** Resource content compressor */
    private ResourceCompressor compressor
    
    /** Servlet context */
    private ServletContext servletContext

    /** Render mutex */
    private Object mutex

    /** Console errors printer (prints render exceptions) */
    private DefaultErrorsPrinter errorsPrinter

    /** Page URL for displaying error screen */
    private static final ERROR_PAGE_URI = '/error.html'

    /**
     * Initializes Grain servlet from Servlet container context.
     * 
     * @param config servlet config
     * 
     * @throws ServletException in case some error occurs
     */
    void init(ServletConfig config) throws ServletException {
        super.init(config)
        def context = config.getServletContext()
        this.urlRegistry = (URLRegistry) context.getAttribute('urlRegistry')
        this.compressor = (ResourceCompressor) context.getAttribute('compressor')
        this.mutex = (Object) context.getAttribute('renderMutex')
        this.servletContext = context
        this.errorsPrinter = new DefaultErrorsPrinter()
        log("GrainServlet initialized")
    }

    /**
     * Services HTTP request by looking up resource by URL in URL - resource mapping, rendering
     * resource and compressing it.
     * 
     * @param request HTTP request
     * @param response HTTP response
     * 
     * @throws ServletException in case servlet error occurs
     * @throws IOException in case IO error occurs
     */
    void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String uri = request.getServletPath()
        String info = request.getPathInfo()
        if (info != null) {
            uri += info
        }

        synchronized (mutex) {
            def resource = urlRegistry.getResource(uri)
            if (!resource) {
                if (urlRegistry.getResource(uri + "/")) {
                    response.setHeader('Location', "$uri/")
                    response.sendError(HttpServletResponse.SC_MOVED_PERMANENTLY)
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND)
                }
            } else {
                    def mimeType = servletContext.getMimeType(uri) ?: (uri.endsWith('/') ? 'text/html' : 'text/plain')
                    response.setContentType("$mimeType; charset=utf-8")
                    if (compressor.gzipEnabled) {
                        response.addHeader("Content-Encoding", "gzip")
                    }
                try {
                    response.getOutputStream().write(compressor.compress(resource.location, resource.render().bytes))
                    response.setStatus(HttpServletResponse.SC_OK)
                } catch (RenderException t) {
                    log(errorsPrinter.printStackTrace(t))
                    log(errorsPrinter.printCodeSnippet(t))
                    resource = urlRegistry.getResource(ERROR_PAGE_URI)
                    if (resource) {
                        // write an error page response
                        response.getOutputStream().write(
                                compressor.compress(resource.location, (resource + [exception: t]).render().bytes))
                    } else {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                    }
                } catch(Throwable t) {
                    log("Failed to render resource", t)
                }
                response.flushBuffer()
            }
        }
    }
}
