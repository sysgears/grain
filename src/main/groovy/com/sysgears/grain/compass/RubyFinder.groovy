package com.sysgears.grain.compass

import javax.inject.Named

/**
 * Finder of most appropriate Ruby version in the system
 */
public class RubyFinder {
    
    /** Ruby command candidates to check */
    private static final def RUBY_CANDIDATES = ["ruby", "ruby1.8.7", "ruby1.9.3", "${System.getProperty('user.home')}/.rvm/bin/ruby"] 
    
    /**
     * Finds most appropriate Ruby command in the system.
     */
    public static String getRubyCmd() {
        RUBY_CANDIDATES.find {
            try {
                [it, '-v'].execute()
            } catch (Throwable ignored) {
                false
            }
        }
    }
}
