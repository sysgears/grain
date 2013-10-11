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

package com.sysgears.grain.expando

import jnr.posix.JavaFileStat
import jnr.posix.POSIXFactory
import jnr.posix.util.DefaultPOSIXHandler

import javax.inject.Named

/**
 * File class additional dynamic methods.
 */
@Named
@javax.inject.Singleton
class FileDynamicMethods {

    /**
     * Registers additional methods for File class
     */
    static void register() {

        /**
         * Returns file creation time.
         */
        File.metaClass.dateCreated = {
            
            def handler = new DefaultPOSIXHandler()
            def posix = POSIXFactory.getJavaPOSIX(handler)
            def fileStat = new JavaFileStat(posix, handler)
            fileStat.setup(delegate.absolutePath)

            return fileStat.ctime() * 1000
        }

        /**
         * Returns file extension
         */
        File.metaClass.getExtension = {
            def name = delegate.name 
            def idx = name.lastIndexOf('.')
            idx == -1 ? '' : name.substring(idx + 1)
        }
    }
}
