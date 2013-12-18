package com.sysgears.grain.init

import javax.inject.Inject
import javax.inject.Named

/**
 * Application initializer, responsible for preparation for Grain launch.
 */
@Named
@javax.inject.Singleton
public class Initializer {

    /** Command-line arguments parser */
    @Inject private CmdlineParser cmdlineParser

    /** Tools extractor */
    @Inject private ToolsExtractor extractor

    /**
     * Initializes Grain and returns Grain settings usable by main application. 
     * 
     * @param args command line arguments
     * 
     * @return Grain settings 
     */
    public GrainSettings run(final String[] args) {
        
        // Parse command-line arguments 
        def settings = cmdlineParser.parse(args)

        // Extract bundled tools
        new ToolsExtractor().extractTools(settings)
        
        settings
    }
}
