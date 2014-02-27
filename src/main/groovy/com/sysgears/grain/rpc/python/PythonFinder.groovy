package com.sysgears.grain.rpc.python

import com.sysgears.grain.config.Config
import com.sysgears.grain.rpc.ShellCommand
import com.sysgears.grain.rpc.ShellCommandFinder

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
    public ShellCommand checkCandidate(String name) {
        try {
            def ver = new StringWriter()
            def proc = [name, '--version'].execute()
            proc.consumeProcessErrorStream(ver).join()
            def command = new ShellCommand(command: name,
                    version: ver.toString().readLines().first().trim(),
                    pkgManager: config.python?.setup_tools ?: '')
            command.version.startsWith('Python 2') ? command : null
        } catch (Throwable ignored) {
            null
        }
    }
}
