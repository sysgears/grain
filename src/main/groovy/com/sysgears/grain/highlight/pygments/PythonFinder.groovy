package com.sysgears.grain.highlight.pygments

/**
 * Finder of most appropriate Python version in the system
 */
public class PythonFinder {
    
    /** Python command candidates to check */
    private static final def PYTHON_CANDIDATES = ["python", "python2.7"] 
    
    /**
     * Finds most appropriate Ruby command in the system.
     */
    public static String getPythonCmd() {
        def candidates = System.getProperty("os.name").toLowerCase().contains('win') ?
            PYTHON_CANDIDATES.collect { it + ".exe" } : PYTHON_CANDIDATES
        candidates.find {
            try {
                [it, '-v'].execute()
            } catch (Throwable ignored) {
                false
            }
        }
    }
}
