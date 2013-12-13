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

package com.sysgears.grain.preview

import com.sysgears.grain.CmdlineOptions
import com.sysgears.grain.config.Config
import groovy.util.logging.Slf4j
import name.pachler.nio.file.*

import javax.inject.Inject
import javax.inject.Named

/**
 * File change watcher.
 * <p>
 * Monitors for site file changes and propagates them to the listeners.
 */
@Named
@javax.inject.Singleton
@Slf4j
class FileWatcher extends Thread {

    /** File watching service */
    private WatchService watchService = FileSystems.default.newWatchService()

    /** Watch key to watch directory map */
    private Map<WatchKey, File> keys = [:]
    
    /** Site config */
    @Inject private Config config

    /** Grain command-line options */
    @Inject private CmdlineOptions opts

    /** Config change broadcaster */
    @Inject private ConfigChangeBroadcaster configChangeBroadcaster

    /** Per-file change broadcaster */
    @Inject private FileChangeBroadcaster fileChangeBroadcaster

    /** Site change broadcaster */
    @Inject private SiteChangeBroadcaster siteChangeBroadcaster

    /** Rendering mutex */
    @Inject @Named("renderMutex") private Object mutex
    
    /** Monitored watch events */
    private static final WatchEvent.Kind[] EVENTS = [
            StandardWatchEventKind.ENTRY_DELETE,
            StandardWatchEventKind.ENTRY_CREATE,
            StandardWatchEventKind.ENTRY_MODIFY]

    /**
     * Watches for changes of site files in a background thread.
     */
    void run() {
        Set<File> sourceDirs = config.source_dir.collect { new File(it as String).absoluteFile }
        Set<File> configDirs = [opts.configFile, opts.globalConfigFile]*.parentFile.absoluteFile

        try {
            sourceDirs.each { File srcDir ->
                if (srcDir.exists()) {
                    log.info "Watching dir ${srcDir} for changes recursively"
                    watchRecursive(srcDir)
                }
            }
            configDirs.each { File srcDir ->
                if (srcDir.exists()) {
                    log.info "Watching dir ${srcDir} for changes non-recursively"
                    watchDir(srcDir)
                }
            }
        } catch (t) {
            t.printStackTrace()
        }

        Set<File> changedFiles = []
        boolean exit = false
        while (!exit) {
            try {
                sleep(800, { exit = true })
                WatchKey signalledKey = watchService.poll()
                if (signalledKey == null) {
                    if (!changedFiles.empty) {
                        synchronized (mutex) {
                            changedFiles.each { file ->
                                log.info "Changed file at ${file}"
                                fileChangeBroadcaster.fileChanged(file)
                            }
                            configChangeBroadcaster.configChanged()
                            siteChangeBroadcaster.siteChanged()
                            log.info 'Done handling changed files'
                            changedFiles.clear()
                        }
                    }
                } else {
                    def watchEvents = signalledKey.pollEvents()
                    watchEvents.each { WatchEvent e ->
                        def relativePath = e?.context()?.toString()
                        if (relativePath != null) {
                            File dir = keys.get(signalledKey)
                            File f = new File(dir, relativePath)
                            if (!configDirs.contains(dir) && f.isDirectory() &&
                                    !keys.containsKey(Paths.get(f.absolutePath))) {
                                log.debug "Registering new dir to watch: ${f.absolutePath}"
                                log.info "Watching dir ${f} for changes recursively"
                                watchRecursive(f)
                                f.eachFileRecurse {
                                    if (!isIgnored(it)) {
                                        changedFiles << it
                                    }
                                }
                            } else {
                                // Exclude from consideration generated files by IntelliJ IDEA (yyy.___jb_bak___, zzz.__jb_old___)
                                // and Mac OS x (._yyy.zzz)
                                if (!isIgnored(f)) {
                                    changedFiles << f
                                }
                            }
                        }
                    }
                    signalledKey.reset()
                }
            } catch (ClosedWatchServiceException t) {
                t.printStackTrace()
                System.exit(1)
            } catch (t) {
                changedFiles.clear()
                t.printStackTrace()
            }
        }
    }

    /**
     * Checks whether file changes should be ignored. E.g. is it a some form of backup file
     * 
     * @param file file to check
     * 
     * @return whether file changes should be ignored
     */
    private static boolean isIgnored(final File f) {
        f.isDirectory() || f.name.endsWith("___jb_bak___") || f.name.endsWith("___jb_old___") ||
        f.name.startsWith("._")
    }

    /**
     * Registers single directory for watching file changes.
     * 
     * @param dir new directory to watch for changes 
     */
    private void watchDir(File dir) {
        try {
            keys.put(Paths.get(dir.absolutePath).register(watchService, EVENTS), dir)
        } catch (t) {
            throw new RuntimeException("Error while registering watch hook for: ${dir.absolutePath}", t)
        }
    }

    /**
     * Adds directory and all subdirectory recursively for watching.
     * 
     * @param dir new directory to watch for changes
     */
    private void watchRecursive(File dir) {
        watchDir(dir)
        dir.eachDirRecurse { File childDir ->
            watchDir(childDir)
        }
    }
}
