package com.sysgears.grain.init

/**
 * Representation of Grain version.
 */
class GrainVersion {

    /* The version numbers. */
    private Map numbers = [major: '', minor: '', revision: '']

    /**
     * Initializes Grain version object.<br />
     * Valid version numbers: '0.5.1', '0.5.+' or '0.5.1-SNAPSHOT'.
     *
     * @param version the string representation of the version number
     */
    public GrainVersion(String version) {
        def groups = [major: 1, minor: 2, revision: 3]
        def matcher = (version =~ /^([0-9]+)\.([0-9]+)\.(([0-9]+)|([0-9]+(-SNAPSHOT)?)|(\+))$/)

        // throws the exception if the version doesn't match the x.y.[z+] format
        if (!matcher) { throw new IllegalArgumentException("Wrong Grain version ${version}, " +
                "expected format: x.y.z, where z can be a number or + sign.") }

        // fills in the version numbers
        groups.each { name, number ->
            numbers[name] = matcher[0][number]
        }
    }

    /**
     * Checks if the versions are interchangeable by comparing the major and minor version numbers.
     *
     * @param version version object to compare this version against
     * @return true if the versions are interchangeable, false otherwise
     */
    public boolean isInterchangeable(GrainVersion version) {
        return numbers.major == version.numbers.major && numbers.minor == version.numbers.minor
    }

    /**
     * Checks if the current version is backward compatible with the specified version.
     *
     * @param version version object to check against
     * @return true if the current version is backward compatible with the specified version, false otherwise
     */
    public boolean isBackwardCompatible(GrainVersion version) {
        return numbers.major == version.numbers.major &&
                numbers.minor.toInteger() >= version.numbers.minor.toInteger()
    }

    /**
     * Returns String representation of Grain version
     *
     * @return String representation of Grain version
     */
    public String toString() {
        "${numbers.major}.${numbers.minor}.${numbers.revision}"
    }
}