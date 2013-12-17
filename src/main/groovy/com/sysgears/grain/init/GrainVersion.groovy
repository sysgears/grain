package com.sysgears.grain.init

/**
 * Representation of Grain version 
 */
class GrainVersion {
    
    /** Major Grain version */
    private int major
    
    /** Middle Grain version */
    private int middle
    
    /** Minor Grain version */
    private int minor
    
    public GrainVersion(String version) {
        def m = version =~ /^([0-9]+)\.([0-9]+)\.([0-9]+)/
        if (!m.find()) {
            throw new IllegalArgumentException("Wrong Grain version ${version}, expected format: x.y.z with optional -SNAPSHOT ending")
        } else {
            major = m[0][1]
            middle = m[0][2]
            minor = m[0][3]
        }
    }

    /**
     * Checks whether current Grain version is backward compatible to the given one
     * 
     * @param version Grain version to check with 
     * 
     * @return is current Grain version backward compatible to the given one 
     */
    public boolean isBackwardCompatibleTo(GrainVersion version) {
        return major == version.major && middle >= version.middle
    }

    /**
     * Returns String representation of Grain version
     * 
     * @return String representation of Grain version 
     */
    public String toString() {
        "${major}.${middle}.${minor}"
    }
}
