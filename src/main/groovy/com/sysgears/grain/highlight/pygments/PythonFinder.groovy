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
        PYTHON_CANDIDATES.find {
            try {
                [it, '-v'].execute()
            } catch (Throwable ignored) {
                false
            }
        }
    }
}
