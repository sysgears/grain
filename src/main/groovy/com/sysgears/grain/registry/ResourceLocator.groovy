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

import com.sysgears.grain.init.CmdlineOptions
import com.sysgears.grain.config.Config

import javax.inject.Inject
import javax.inject.Named

/**
 * Provides mechanism for locating site resources
 */
@Named
@javax.inject.Singleton
class ResourceLocator {
    
    /** Site config */
    @Inject private Config config

    /** Command-line options */
    @Inject private CmdlineOptions opts

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
            if (opts.command == 'preview') {
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
            if (opts.command == 'preview') {
                null
            } else {
                throw new RuntimeException("Unable to find include: ${location}")
            }
        } else {
            new File(dirStr.toString(), location)
        }
    }
}
