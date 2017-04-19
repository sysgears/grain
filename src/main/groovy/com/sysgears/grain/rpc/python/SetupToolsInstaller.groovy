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
    private static final String VERSION = '8.2'

    /** Grain settings */
    @Inject private GrainSettings settings

    /**
     * Downloads and install Python setuptools into Grain local cache.
     *
     * @return installed setuptools egg path
     */
    public String install(String version = VERSION) {
        def setupToolsDir = "${settings.grainHome}/packages/python/setuptools"
        def setupToolsEgg = new File("${settings.grainHome}/packages/python/setuptools/setuptools-${version}.egg")
        if (!setupToolsEgg.exists()) {
            def tempDir = File.createTempDir()
            try {
                def tarballName = "setuptools-${version}.tar.gz"
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
                new File(setupDir, 'setuptools.pth') << "./setuptools-${version}.egg\n"
                def eggDir = new File(setupDir, "setuptools-${version}.egg")
                eggDir.mkdir()

                def sourceDir = new File(tempDir, "setuptools-${version}")

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
                    move(todir: setupToolsDir) {
                        fileset(dir: setupDir)
                    }
                }
            } finally {
                FileUtils.deleteDirectory(tempDir)
            }
        } else {
            def confFile = new File(setupToolsDir, 'setuptools.pth')
            def currentVersion = confFile.readLines().find { it =~ ~/setuptools-/ }?.find(/setuptools-(.*).egg/) {
                match, v -> v }
            if (currentVersion != version) {
                confFile.newWriter()
                confFile << "./setuptools-${version}.egg\n"
            }
        }
        new File("${setupToolsDir}/setuptools-${version}.egg").canonicalPath
    }
}
