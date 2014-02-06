package com.sysgears.grain.rpc

import com.sysgears.grain.preview.ConfigChangeListener

import javax.annotation.Nonnull
import javax.annotation.Nullable

/**
 * Search for a command among a user-configurable list of candidates.
 */
abstract class ShellCommandFinder implements ConfigChangeListener {

    /** Shell command candidate currently used */
    @Nullable
    private ShellCommand currentCandidate
    
    /** Whether we attempted to search for candidate after instantiation */
    private boolean initialized = false

    /**
     * @inheritDoc
     */
    @Override
    void configChanged() {
        currentCandidate = findCandidate()
    }

    /**
     * Returns most appropriate command in the system.
     */
    @Nullable
    public ShellCommand getCmd() {
        if (!initialized) {
            currentCandidate = findCandidate()
            initialized = true
        }
        
        currentCandidate
    }

    /**
     * Finds most appropriate command in the system.
     *  
     * @return command 
     */
    private ShellCommand findCandidate() {
        def candidates = userConfiguredCandidates + defaultCandidates

        candidates = isWindows() ? candidates.collect { it + ".exe" } : candidates
        candidates.findResult { checkCandidate(it.toString()) }
    }

    /**
     * Detects whether we are running under Windows.
     * 
     * @return whether we are running under Windows.
     */
    private static boolean isWindows() {
        System.getProperty("os.name").toLowerCase().contains('win')
    }

    /**
     * Returns default candidates for shell command from SiteConfig.groovy 
     *
     * @return default candidates for shell command 
     */
    @Nonnull
    abstract List<String> getDefaultCandidates()

    /**
     * Returns user configured candidates for shell command from SiteConfig.groovy 
     * 
     * @return user configured candidates
     */
    @Nonnull
    abstract List<String> getUserConfiguredCandidates()
    
    /**
     * Checks candidate and returns whether it is accepted or not.
     * 
     * @return candidate command or null
     */
    @Nonnull
    abstract ShellCommand checkCandidate(String name)
}
