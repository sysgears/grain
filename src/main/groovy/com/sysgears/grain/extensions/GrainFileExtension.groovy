package com.sysgears.grain.extensions

import jnr.posix.JavaFileStat
import jnr.posix.POSIXFactory
import jnr.posix.util.DefaultPOSIXHandler



/**
 * File extension methods
 */
class GrainFileExtension {

    /**
     * Get date of file creation. Uses native operating system library.
     * @param self File instance
     * @return date in millis.
     */
    static long dateCreated(File self) {
        def handler = new DefaultPOSIXHandler()
        def posix = POSIXFactory.getJavaPOSIX(handler)
        def fileStat = new JavaFileStat(posix, handler)
        fileStat.setup(self.absolutePath)
        fileStat.ctime() * 1000
    }

    /**
     * Get file's extension.
     * @param self String instance.
     * @return a String with file's extension,
     * or an empty String if file has no extension.
     */
    static String getExtension(File self) {
        def name = self.name
        def idx = name.lastIndexOf('.')
        idx == -1 ? '' : name.substring(idx + 1)
    }
}
