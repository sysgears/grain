package com.sysgears.grain.rpc.ruby

import com.sysgears.grain.config.Config
import com.sysgears.grain.rpc.ShellCommand
import com.sysgears.grain.rpc.ShellCommandFinder

import javax.inject.Inject

/**
 * Finder of most appropriate Ruby version in the system
 */
@javax.inject.Singleton
public class GemFinder extends ShellCommandFinder {

    /** Site config */
    @Inject private Config config

    /**
     * @inheritDoc
     */
    @Override
    List<String> getDefaultCandidates() {
        ["gem", "${System.getProperty('user.home')}/.rvm/bin/gem"]
    }

    /**
     * @inheritDoc
     */
    @Override
    List<String> getUserConfiguredCandidates() {
        []
    }

    /**
     * @inheritDoc
     */
    @Override
    public ShellCommand checkCandidate(String name) {
        try {
            def ver = new StringWriter()
            def proc = "${name} --version".execute()
            proc.consumeProcessOutputStream(ver).join()
            new ShellCommand(command: name,
                    version: ver.toString().readLines().first().trim())
        } catch (Throwable ignored) {
            null
        }
    }
}
