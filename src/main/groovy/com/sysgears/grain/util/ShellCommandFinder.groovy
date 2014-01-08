package com.sysgears.grain.util

import com.sysgears.grain.config.Config
import com.sysgears.grain.preview.ConfigChangeListener

import javax.annotation.Nonnull
import javax.annotation.Nullable

/**
 * Search for a commaand among a user-configurable list of candidates.
 */
abstract class ShellCommandFinder implements ConfigChangeListener {


    @Nullable
    private String currentCandidate

    protected final Config config

    protected ShellCommandFinder(Config config) {
        this.config = config
        findCandidate()
    }

    /**
     * Finds most appropriate command in the system.
     */
    @Nullable
    public String getCmd() {
        currentCandidate
    }

    private String findCandidate() {
        def candidates = []

        candidates += getUserConfiguredCandidates()
        candidates += getDefaultCandidates()

        candidates = isWindows() ? candidates.collect { it + ".exe" } : candidates
        currentCandidate = candidates.find {
            try {
                [it, getArg()].execute()
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

    @Nonnull
    abstract List<String> getDefaultCandidates()

    @Nonnull
    abstract List<String> getUserConfiguredCandidates()

    /**
     * @return a command line arg to supply to each candidate. Doesn't matter what it does as long as it will always
     * succeed (e.g. -v for version).
     */
    @Nonnull
    abstract String getArg()
}
