package com.sysgears.grain.util

import org.codehaus.groovy.reflection.ReflectionCache

/**
 * Various methods to work with Groovy closures.
 */
class ClosureUtils {

    /**
     * Returns the minimum number of parameters a doCall method of the closure can take.
     *
     * @return the number of required parameters
     */
    static def getMinArgs = { Closure c ->
        ReflectionCache.getCachedClass(c.class).methods
                .findAll { 'doCall'.equals(it.name) }*.nativeParameterTypes.collect { it.size() }.min()
    }

    /**
     * Returns the maximum number of parameters a doCall method of the closure can take.
     *
     * @return the number of required and optional parameters
     */
    static def getMaxArgs = { Closure c ->
        c.maximumNumberOfParameters
    }
}
