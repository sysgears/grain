package com.sysgears.grain.rpc.python

import com.sysgears.grain.init.GrainSettings
import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils

import javax.inject.Inject

/**
 * Downloads and install Python setuptools into Grain local cache
 */
@javax.inject.Singleton
@Slf4j
public class SetupToolsInstaller {

    /** SetupTools version to use */
    private static final String VERSION = '2.1'

    /** Grain settings */
    @Inject private GrainSettings settings

    /**
     * Downloads and install Python setuptools into Grain local cache.
     * 
     * @return installed setuptools egg path
     */
    public String install() {
        def setupToolsDir = new File("${settings.grainHome}/packages/python/setuptools")
        if (!setupToolsDir.exists()) {
            def tempDir = File.createTempDir()
            try {
                def tarballName = "setuptools-${VERSION}.tar.gz"
                def baseUrl = 'https://pypi.python.org/packages/source/s/setuptools/'
                def tarball = new File(tempDir, tarballName)
                def ant = new AntBuilder()
                if (!log.debugEnabled && !log.traceEnabled) {
                    ant.project.getBuildListeners().firstElement().setMessageOutputLevel(0)
                }
                def url = new URL(baseUrl + tarballName)
                log.info "Downloading ${url}..."
                tarball << url.openStream()
    
                def setupDir = new File(tempDir, 'setuptools')
                setupDir.mkdir()
                new File(setupDir, 'setuptools.pth') << "./setuptools-${VERSION}.egg\n"
                def eggDir = new File(setupDir, "setuptools-${VERSION}.egg")
                eggDir.mkdir()
    
                def sourceDir = new File(tempDir, "setuptools-${VERSION}")
    
                ant.sequential() {
                    untar(src: tarball, dest: tempDir, compression: 'gzip')
                    copy(todir:"${eggDir}/EGG-INFO") {
                        fileset(dir: "${sourceDir}/setuptools.egg-info")
                    }
                    copy(todir:"${eggDir}/_markerlib") {
                        fileset(dir: "${sourceDir}/_markerlib")
                    }
                    copy(todir:"${eggDir}/setuptools") {
                        fileset(dir: "${sourceDir}/setuptools")
                    }
                    copy(todir:"${eggDir}") {
                        fileset(dir: sourceDir) {
                            include(name: "easy_install.py")
                            include(name: "pkg_resources.py")
                        }
                    }
                    move(todir:"${settings.grainHome}/packages/python/setuptools") {
                        fileset(dir: setupDir)
                    }
                }
            } finally {
                FileUtils.deleteDirectory(tempDir)
            }
        }
        new File("${setupToolsDir}/setuptools-${VERSION}.egg").canonicalPath
    }
}
