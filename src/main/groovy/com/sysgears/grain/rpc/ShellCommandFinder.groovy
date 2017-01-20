package com.sysgears.grain.rpc

import com.sysgears.grain.preview.ConfigChangeListener
import com.sysgears.grain.service.Service
import com.sysgears.grain.service.ServiceManager
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable


/**
 * Search for a command among a user-configurable list of candidates.
 */
abstract class ShellCommandFinder implements ConfigChangeListener, Service {

    /** Shell command candidate currently used */
    @Nullable
    private ShellCommand currentCandidate

    /**
     * @inheritDoc
     */
    public void start() {
    }

    /**
     * @inheritDoc
     */
    public void stop() {
    }

    /**
     * @inheritDoc
     */
    @Override
    void configChanged() {
        def candidate = findCandidate()
        if (currentCandidate?.command != candidate?.command ||
                currentCandidate?.pkgManager != candidate?.pkgManager) {
            ServiceManager.stopService(this)
            currentCandidate = candidate
        }
    }

    /**
     * Returns most appropriate command in the system.
     */
    @Nullable
    public ShellCommand getCmd() {
        currentCandidate
    }

    /**
     * Finds most appropriate command in the system.
     *
     * @return command
     */
    private ShellCommand findCandidate() {
        def candidates = userConfiguredCandidates + defaultCandidates

        candidates = isWindows() ? candidates.collect { "cmd /c ${it}" } : candidates
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
    @NotNull
    abstract List<String> getDefaultCandidates()

    /**
     * Returns user configured candidates for shell command from SiteConfig.groovy 
     *
     * @return user configured candidates
     */
    @NotNull
    abstract List<String> getUserConfiguredCandidates()

    /**
     * Checks candidate and returns whether it is accepted or not.
     *
     * @return candidate command or null
     */
    @NotNull
    abstract ShellCommand checkCandidate(String name)
}
