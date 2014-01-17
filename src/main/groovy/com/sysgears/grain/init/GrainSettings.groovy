/*
 * Copyright (c) 2013 SysGears, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sysgears.grain.init

/**
 * Grain settings
 */
class GrainSettings {
    
    /** Config file of the site. */
    File configFile

    /** Global config file. */
    File globalConfigFile
    
    /** Location of directory with external tools files */ 
    File toolsHome
    
    /** Grain home location */
    File grainHome

    /** Environment of the site (dev, prod, ..) */
    String env

    /** Command to be executed. */
    String command

    /** Command-specific options */ 
    List<String> args

    /** Grain version */
    String grainVersion
    
    /** Grain command-line builder */
    CliBuilder cliBuilder
    
    /** Whether Grain should show usage and exit */
    boolean showUsageAndExit
}
