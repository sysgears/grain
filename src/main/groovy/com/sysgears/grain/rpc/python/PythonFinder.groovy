package com.sysgears.grain.rpc.python

import com.sysgears.grain.config.Config
import com.sysgears.grain.util.ShellCommandFinder

import javax.inject.Inject

/**
 * Finder of most appropriate Python version in the system
 */
@javax.inject.Singleton
public class PythonFinder extends ShellCommandFinder {

    /** Site config */
    @Inject private Config config

    /**
     * @inheritDoc
     */
    @Override
    public List<String> getDefaultCandidates() {
        ['python2', "python", "python2.7"]
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<String> getUserConfiguredCandidates() {
        config.python?.cmd_candidates ?: []
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean checkCandidate(String name) { 
        try {
            def proc = [name, '--version'].execute()
            def err = new StringWriter()
            proc.consumeProcessErrorStream(err).join()
            err.toString().startsWith('Python 2')
        } catch (Throwable ignored) {
            false
        }
    }
}
