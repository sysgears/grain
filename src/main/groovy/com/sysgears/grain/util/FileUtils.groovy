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

package com.sysgears.grain.util

/**
 * Various miscellaneous methods to work with files and directories. 
 */
class FileUtils {

    /**
     * Creates specified directories
     * 
     * @param dirs target directory list
     * 
     * @throws IOException if one of target directories cannot be created 
     */
    public static void createDirs(File... dirs) throws IOException {
        dirs.each { dir ->
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IOException("Failed to create $dir")
            }
        }
    }

    /**
     * Removes given directory recursively.
     * 
     * @param dir directory to remove
     * 
     * @throws IOException if one of files in the directory cannot be removed 
     */
    public static void removeDir(File dir) throws IOException {
        if (dir.isDirectory()) {
            dir.listFiles().each { c ->
                if (c.isDirectory()) {
                    if (!c.deleteDir()) {
                        throw new IOException("Unable to remove subdirectory ${c} in ${dir}")
                    }
                } else {
                    if (!c.delete()) {
                        throw new IOException("Unable to remove file ${c} in ${dir}")
                    }
                }
            }
            if (!dir.delete()) {
                throw new IOException("Unable to remove directory ${dir}")
            }
        }
    }

}
