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
import com.sysgears.grain.init.GrainSettings

import javax.inject.Inject

/**
 * Provides mechanism for locating site resources
 */
@javax.inject.Singleton
class ResourceLocator {
    
    /** Site config */
    @Inject private Config config

    /** Grain settings */
    @Inject private GrainSettings settings

    /**
     * Checks whether specified file is non-excluded resource file 
     *
     * @param file a file
     *
     * @return whether specified file is non-excluded resource file
     */
    public boolean isResource(File file) {
        (!file.exists() || file.isFile()) &&
                ([] + config.include_dir).find { file.path.startsWith(new File(it.toString()).absolutePath) } == null &&
                ([] + config.layout_dir).find { file.path.startsWith(new File(it.toString()).absolutePath) } == null &&
                !isExcluded(file)
    }

    /**
     * Returns canonical location of the resource given it's file
     * 
     * @param resourceFile resource file
     * 
     * @return canonical relative location of the resource within the site
     */
    public String getLocation(File resourceFile) {
        List dirs = [] + config.source_dir
        def res = resourceFile.canonicalPath
        dirs.each {
            res = res - new File(it.toString()).canonicalPath
        }
        if (res == resourceFile.canonicalPath) {
            // The file does not belong to any of the source dirs.
            ""
        } else {
            res.replace('\\', '/')
        }
    }

    /**
     * Returns layout file by location given
     *
     * @param location layout location
     *
     * @return layout file
     */
    public File findLayout(String location) {
        List dirs = [] + config.layout_dir
        if (!location.contains('.')) {
            location = "${location}.html"
        }
        def dirStr = dirs.find {
            def file = new File(it.toString(), location)
            file.exists() && file.isFile()
        }
        if (!dirStr) {
            if (settings.command == 'preview') {
                null
            } else {
                throw new RuntimeException("Unable to find layout: ${location}")
            }
        } else {
            new File(dirStr.toString(), location)
        }
    }

    /**
     * Returns include file by location given
     *
     * @param location include location
     *
     * @return include file
     */
    public File findInclude(String location) {
        List dirs = [] + config.include_dir + config.source_dir
        def dirStr = dirs.find {
            def file = new File(it.toString(), location)
            file.exists() && file.isFile()
        }
        if (!dirStr) {
            if (settings.command == 'preview') {
                null
            } else {
                throw new RuntimeException("Unable to find include: ${location}")
            }
        } else {
            new File(dirStr.toString(), location)
        }
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
            def location = getLocation(file)
            excludes.find { String pattern ->
                location.matches(pattern)
            } != null
        } else {
            return false
        }
    }

}
