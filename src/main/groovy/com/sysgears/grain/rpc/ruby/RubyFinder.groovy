package com.sysgears.grain.rpc.ruby

import com.sysgears.grain.config.Config
import com.sysgears.grain.rpc.ShellCommand
import com.sysgears.grain.rpc.ShellCommandFinder

import javax.inject.Inject

/**
 * Finder of most appropriate Ruby version in the system
 */
@javax.inject.Singleton
public class RubyFinder extends ShellCommandFinder {

    /** Site config */
    @Inject private Config config

    /**
     * @inheritDoc
     */
    @Override
    List<String> getDefaultCandidates() {
        ["ruby", "ruby1.8.7", "ruby1.9.3", "${System.getProperty('user.home')}/.rvm/bin/ruby", "${System.getProperty('user.home')}/.rvm/bin/ruby-rvm-env"]
    }

    /**
     * @inheritDoc
     */
    @Override
    List<String> getUserConfiguredCandidates() {
        config.ruby?.cmd_candidates ?: []
    }

    /**
     * @inheritDoc
     */
    @Override
    public ShellCommand checkCandidate(String name) {
        try {
            def ver = new StringWriter()
            def proc = [name, '--version'].execute()
            proc.consumeProcessOutputStream(ver).join()
            new ShellCommand(command: name,
                    version: ver.toString().readLines().first().trim(),
                    pkgManager: config.ruby?.ruby_gems ?: '')
        } catch (Throwable ignored) {
            null
        }
    }
}
