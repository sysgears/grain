package com.sysgears.grain.rpc.ruby

import com.sysgears.grain.init.GrainSettings
import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils

import javax.inject.Inject

/**
 * Downloads and install RubyGems into Grain local cache
 */
@javax.inject.Singleton
@Slf4j
public class RubyGemsInstaller {

    /** RubyGems version to use */
    private static final String VERSION = '2.2.2'

    /** Grain settings */
    @Inject private GrainSettings settings

    /**
     * Downloads and install Python setuptools into Grain local cache.
     * 
     * @return installed setuptools egg path
     */
    public String install() {
        def rubyGemsDir = new File("${settings.grainHome}/packages/ruby/rubygems-${VERSION}")
        if (!rubyGemsDir.exists()) {
            def tempDir = File.createTempDir()
            try {
                def tarballName = "v${VERSION}.tar.gz"
                def baseUrl = 'https://github.com/rubygems/rubygems/archive/'
                def tarball = new File(tempDir, tarballName)
                def ant = new AntBuilder()
                if (!log.debugEnabled && !log.traceEnabled) {
                    ant.project.getBuildListeners().firstElement().setMessageOutputLevel(0)
                }
                def url = new URL(baseUrl + tarballName)
                log.info "Downloading ${url}..."
                tarball << url.openStream()
    
                def sourceDir = new File(tempDir, "rubygems-${VERSION}")
    
                ant.sequential() {
                    untar(src: tarball, dest: tempDir, compression: 'gzip')
                    copy(todir:"${rubyGemsDir}") {
                        fileset(dir: "${sourceDir}/lib")
                    }
                }
            } finally {
                FileUtils.deleteDirectory(tempDir)
            }
        }
        "${rubyGemsDir}"
    }
}
