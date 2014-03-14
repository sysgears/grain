package com.sysgears.grain.clean

import com.sysgears.grain.config.Config
import com.sysgears.grain.log.StreamLoggerFactory
import com.sysgears.grain.util.FileUtils
import groovy.util.logging.Slf4j

import javax.inject.Inject

/**
 * Grain directories cleaner
 */
@javax.inject.Singleton
@Slf4j
class GrainCleaner {

    /** Site config. */
    @Inject private Config config

    /** Stream logger factory. */
    @Inject private StreamLoggerFactory streamLoggerFactory

    /**
     * Cleans Grain temporary directories.
     */
    public void clean() {
        log.info 'Cleaning cache and destination directories...'
        def cacheDir = new File(config.cache_dir.toString())
        def destDir = new File(config.destination_dir.toString())
        FileUtils.removeDir(cacheDir)
        FileUtils.removeDir(destDir)
        log.info 'Grain temporary directories have been cleaned.'
    }
}
