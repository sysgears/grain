package com.sysgears.grain.highlight.pygments

import com.sysgears.grain.config.Config
import com.sysgears.grain.preview.ConfigChangeListener

import javax.annotation.Nullable
import javax.inject.Inject
import javax.inject.Named

/**
 * Finder of most appropriate Python version in the system
 */
@Named
@javax.inject.Singleton
public class PythonFinder implements ConfigChangeListener {

    /** Python command candidates to check */
    private static final def DEFAULT_PYTHON_CANDIDATES = ['python2', "python", "python2.7"]

    @Nullable
    private String currentCandidate

    private final Config config

    @Inject
    PythonFinder(Config config) {
        this.config = config
        findCandidate()
    }

    /**
     * Finds most appropriate Python command in the system.
     */
    @Nullable
    public String getPythonCmd() {
        currentCandidate
    }

    private String findCandidate() {
        def candidates = []

        if (config.features?.python?.cmd_candidates) {
            candidates += config.python.cmd_candidates as List 
        }

        candidates += DEFAULT_PYTHON_CANDIDATES
        candidates = isWindows() ? candidates.collect { it + ".exe" } : candidates
        currentCandidate = candidates.find {
            try {
                [it, '-v'].execute()
            } catch (Throwable ignored) {
                false
            }
        }
    }

    private static boolean isWindows() {
        System.getProperty("os.name").toLowerCase().contains('win')
    }

    @Override
    void configChanged() {
        findCandidate()
    }
}
