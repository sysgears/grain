package com.sysgears.grain.rpc.ruby

import com.sysgears.grain.config.Config
import com.sysgears.grain.util.ShellCommandFinder

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
        ["ruby", "ruby1.8.7", "ruby1.9.3", "${System.getProperty('user.home')}/.rvm/bin/ruby"]
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
    public boolean checkCandidate(String name) {
        try {
            [name, '--version'].execute()
            true
        } catch (Throwable ignored) {
            false
        }
    }
}
