package com.sysgears.grain.highlight.pygments

import com.sysgears.grain.config.Config
import com.sysgears.grain.util.ShellCommandFinder

import javax.inject.Inject

/**
 * Finder of most appropriate Python version in the system
 */
@javax.inject.Singleton
public class PythonFinder extends ShellCommandFinder {

    @Inject
    protected PythonFinder(Config config) {
        super(config)
    }

    @Override
    List<String> getDefaultCandidates() {
        ['python2', "python", "python2.7"]
    }

    @Override
    List<String> getUserConfiguredCandidates() {
        if (config.features?.python?.cmd_candidates) {
            return config.features.python.cmd_candidates
        }

        []
    }

    @Override
    String getArg() {
        '-v'
    }
}
