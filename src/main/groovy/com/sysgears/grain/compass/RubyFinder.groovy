package com.sysgears.grain.compass

import com.sysgears.grain.config.Config
import com.sysgears.grain.util.ShellCommandFinder

import javax.inject.Inject

/**
 * Finder of most appropriate Ruby version in the system
 */
@javax.inject.Singleton
public class RubyFinder extends ShellCommandFinder {

    @Inject
    protected RubyFinder(Config config) {
        super(config)
    }

    @Override
    List<String> getDefaultCandidates() {
        return ["ruby", "ruby1.8.7", "ruby1.9.3", "${System.getProperty('user.home')}/.rvm/bin/ruby"]
    }

    @Override
    List<String> getUserConfiguredCandidates() {
        if (config.features?.ruby?.cmd_candidates) {
            return config.features.ruby.cmd_candidates
        }

        []
    }

    @Override
    String getArg() {
        return '-v'
    }

}
