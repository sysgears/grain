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

package com.sysgears.grain.registry

import com.sysgears.grain.config.Config
import com.sysgears.grain.service.Service
import com.sysgears.grain.preview.FileChangeListener
import com.sysgears.grain.render.TemplateEngine
import groovy.io.FileType
import groovy.util.logging.Slf4j

import javax.inject.Inject

/**
 * Grain resource registry
 */
@javax.inject.Singleton
@Slf4j
class Registry implements FileChangeListener, Service {
    
    /** Resource locator */
    @Inject private ResourceLocator locator

    /** Resource header parser */
    @Inject private HeaderParser headerParser

    /** Site config */
    @Inject private Config config

    /** Rendering engine */
    @Inject private TemplateEngine templateEngine

    /** Site pages registry */
    private final Map<File, Map<String, Object>> pages = [:]

    /** Site assets registry */
    private final Map<File, Map<String, Object>> assets = [:]
    
    /**
     * Map of on what layouts does each resource depends.
     */
    private Map<File, Collection<File>> resourceDeps = [:]

    /**
     * Map of which resources depend on a given layout.
     */
    private Map<File, Collection<File>> layoutDeps = [:]

    /**
     * Compiles all the site resources (optional step) 
     */
    public void compile() {
        log.info "Starting filling up template cache"
        def sourceDirs = (config.source_dir as List<String>).collect { new File(it) }.reverse()
        sourceDirs*.eachFileRecurse(FileType.FILES) {
            if (isResource(it)) {
                templateEngine.createTemplate(it)
            }
        }
        log.info "Finished filling up template cache"
    }

    /**
     * Starts registry service.
     */
    @Override
    public void start() {
        log.info "Starting resource scan"
        def sourceDirs = (config.source_dir as List<String>).collect { new File(it) }.reverse()
        sourceDirs*.eachFileRecurse(FileType.FILES) {
            if (isResource(it)) {
                add(it)
            }
        }
        log.info "Finished resource scan"
    }

    /**
     * Stops registry service.
     */
    @Override
    public void stop() {
    }
    
    /**
     * Returns all the pages in the registry
     * 
     * @return all the pages in the registry
     */
    public Collection<Map<String, Object>> getPages() {
        pages.values()
    }

    /**
     * Returns all the assets in the registry
     *
     * @return all the assets in the registry
     */
    public Collection<Map<String, Object>> getAssets() {
        assets.values()
    }

    /**
     * Returns all the resources in the registry
     *
     * @return all the resources in the registry
     */
    public Collection<Map<String, Object>> getResources() {
        assets.values() + pages.values()
    }

    /**
     * @inheritDoc 
     */
    @Override
    public void fileChanged(File file) {
        if (!file.exists()) {
            resourceDeps[file].each {
                layoutDeps[it].remove(file)
            }
            resourceDeps.remove(file)
            layoutDeps.remove(file)
            pages.remove(file)
            assets.remove(file)
        } else {
            layoutDeps[file].each {
                add(it)
            }
            if (isResource(file)) {
                add(file)
            }
        }
    }

    /**
     * Adds resource to the registry 
     *
     * @param resourceFile resource file 
     */
    private void add(final File resourceFile) {
        log.debug "Adding file ${resourceFile} to registry"
        def location = locator.getLocation(resourceFile)
        def defaultUrl = location.replaceAll(/\/index\.(md|markdown|html)$/, '/')
        def page = resourceFile.getExtension() in ['html', 'markdown', 'md', 'xml', 'rst', 'adoc', 'asciidoc']
        def resourceConfig = [:]

        if (page) {
            def resourceParser = new ResourceParser(resourceFile)
            resourceConfig = headerParser.parse(resourceFile, resourceParser.header)
            resourceConfig.text = { resourceParser.content }
        }
        resourceConfig.dateCreated = resourceFile.dateCreated()
        resourceConfig.lastUpdated =  resourceFile.lastModified()
        resourceConfig.url = defaultUrl
        resourceConfig.location = location
        resourceConfig.bytes = { resourceFile.bytes }
        resourceConfig.type = page ? 'page' : 'asset'
        def layoutConfig = resourceConfig
        resourceDeps[resourceFile] = []
        while (layoutConfig.layout) {
            def layoutFile = locator.findLayout(layoutConfig.layout)
            if (layoutFile != null) {
                layoutConfig = headerParser.parse(layoutFile)
                resourceDeps[resourceFile] += layoutFile
                if (!layoutDeps[layoutFile]) {
                    layoutDeps[layoutFile] = []
                }
                layoutDeps[layoutFile] += resourceFile
                resourceConfig = layoutConfig + resourceConfig
            } else {
                log.warn "WARNING: ${resourceConfig.location} tried to use absent layout: ${layoutConfig.layout}"
                layoutConfig = [:]
            }
        }
        if (page) {
            pages[resourceFile] = Collections.unmodifiableMap(resourceConfig)
        } else {
            assets[resourceFile] = Collections.unmodifiableMap(resourceConfig)
        }
    }

    /**
     * Checks whether specified file is non-excluded resource file 
     *
     * @param file a file
     *
     * @return whether specified file is non-excluded resource file
     */
    private boolean isResource(File file) {
        (!file.exists() || file.isFile()) &&
                ([] + config.include_dir).find { file.path.startsWith(new File(it.toString()).absolutePath) } == null &&
                ([] + config.layout_dir).find { file.path.startsWith(new File(it.toString()).absolutePath) } == null &&
                !isExcluded(file)
    }

    /**
     * Checks whether resource was excluded by config rules
     *
     * @param file resource file
     *
     * @return whether the file was excluded by config rules
     */
    private boolean isExcluded(File file) {
        def excludes = (config.excludes ?: []) + ['/SiteConfig.groovy', '']
        if (excludes) {
            def location = locator.getLocation(file)
            excludes.find { String pattern ->
                location.matches(pattern)
            } != null
        } else {
            return false
        }
    }
}
