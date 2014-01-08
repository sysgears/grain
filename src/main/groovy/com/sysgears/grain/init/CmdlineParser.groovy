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

import java.util.jar.Manifest

/**
 * Command line arguments parser and validator.
 * <p>
 * In case of facing wrong cmdline arguments dumps help and shutdowns the application
 */
@javax.inject.Singleton
class CmdlineParser {

    /** Default Site config file name */
    private static final String CONFIG_FILE_NAME = 'SiteConfig.groovy'

    /** Default global config file name */
    private static final String GLOBAL_CONFIG_FILE_NAME = 'GlobalConfig.groovy'

    /** Default properties file name */
    private static final String PROPERTIES_FILE_NAME = 'application.properties'

    /**
     * Parses and validates command line arguments.
     * <p>
     * In case of detecting wrong cmdline arguments dumps help and shutdowns the application 
     *
     * @param args command line arguments 
     *
     * @return parsed command line options
     */
    public GrainSettings parse(String[] args) {
        def cli = new CliBuilder(usage: 'site [command] [options]',
                header: '''Commands:
help                      Print out this help
preview                   Run the site in preview mode on local web server
generate                  Generate site from source
deploy                    Deploy the static site
Options:
''')
        cli.with {
            e longOpt: 'env', args: 1, argName: 'environment', 'Override environment used for site config'
        }

        def opts = new GrainSettings(cliBuilder: cli, showUsageAndExit: false)

        def arguments = args.toList()

        def configFile = new File(CONFIG_FILE_NAME)
        if (!configFile.exists() || !configFile.isFile()) {
            System.err.println("Unable to locate config file: ${configFile.canonicalPath}")
            cli.usage()
            System.exit(0)
        }
        
        opts.configFile = configFile.canonicalFile
        opts.globalConfigFile = new File("${System.getProperty('user.home')}/.grain",
                GLOBAL_CONFIG_FILE_NAME)

        def commands = arguments.takeWhile { !it.startsWith('-') }
        arguments = arguments - commands

        opts.command = commands.size() ? commands.remove(0) : 'preview'

        def optionAccessor = cli.parse(arguments)
        if (optionAccessor.e) {
            opts.env = optionAccessor.e
        } else {
            switch (opts.command) {
                case 'help':
                    cli.usage()
                    opts.showUsageAndExit = true
                    opts.env = 'cmd'
                    break
                case 'preview':
                    opts.env = 'dev'
                    break
                case ['generate', 'deploy', 'gendeploy']:
                    opts.env = 'prod'
                    break
                default:
                    opts.env = 'cmd'
                    break
            }
        }

        def grainVersion = getGrainVersion()

        if (!opts.showUsageAndExit)
            validateGrainVersion(grainVersion)

        opts.grainVersion = grainVersion

        opts.toolsHome = getToolsHome()

        opts.args = commands
        
        if (opts.showUsageAndExit) {
            opts.env = 'cmd'
        }
        opts
    }

    /**
     * Detects tools home directory 
     *
     * @return Grain tools home directory
     */
    private static File getToolsHome() {
        def toolsHome

        def toolsHomeProperty = System.getProperty('tools.home')
        if (toolsHomeProperty) {
            toolsHome = new File(toolsHomeProperty).canonicalFile
        } else {
            def classPath = getCurrentClassPath()

            if (!classPath.startsWith('jar')) {
                toolsHome = getFileInDevMode('tools')

                if (!toolsHome) {
                    throw new RuntimeException('Unable to guess Grain tools home, please set tools.home system property')
                }
            } else {
                def manifestPath = classPath.substring(0, classPath.lastIndexOf('!') + 1) +
                        '/META-INF/MANIFEST.MF'
                def manifest = new Manifest(new URL(manifestPath).openStream())
                def attr = manifest.getMainAttributes()
                def rev = attr.getValue('Built-Rev')

                toolsHome = new File(System.getProperty('user.home'), ".grain/tools/${rev}")
            }
        }

        toolsHome
    }

    /**
     * Detects Grain version
     *
     * @return Grain version
     */
    private static String getGrainVersion() {
        def classPath = getCurrentClassPath()

        def grainPropertiesStream

        if (!classPath.startsWith('jar')) {
            def grainPropertiesFile = getFileInDevMode('src/main/resources/application.properties')
            if (grainPropertiesFile && grainPropertiesFile.exists()) {
                grainPropertiesStream = new FileInputStream(grainPropertiesFile)
            } else {
                throw new RuntimeException('Unable to locate Grain properties file')
            }
        } else {
            def grainPropsPath = classPath.substring(0, classPath.lastIndexOf('!') + 1) +
                    '/application.properties'

            grainPropertiesStream = new URL(grainPropsPath).openStream()
        }

        def grainProps = new Properties()
        grainProps.load(grainPropertiesStream)
        def grainVersion = grainProps.getProperty('grain.version')

        if (!grainVersion) {
            throw new RuntimeException('Unable to find which Grain version is used')
        }

        grainVersion
    }

    /**
     * Validates Grain version specified in theme.
     *
     * @param grainVersion current running Grain version
     */
    private static validateGrainVersion(String grainVersion) {
        def propertiesFile = new File(PROPERTIES_FILE_NAME)
        if (!propertiesFile.exists() || !propertiesFile.isFile()) {
            throw new RuntimeException("Unable to locate properties file: ${propertiesFile.canonicalPath}")
        }

        def themeProps = new Properties()
        themeProps.load(new FileInputStream(propertiesFile))
        def themeGrainVersion = themeProps.getProperty('grain.version')

        if (!themeGrainVersion) {
            throw new RuntimeException("Grain version not specified in properties file: ${propertiesFile.canonicalPath}")
        }

        if (!new GrainVersion(grainVersion).isBackwardCompatibleTo(new GrainVersion(themeGrainVersion))) {
            throw new RuntimeException("""Grain versions are not compatible: \nGrain version of the theme: ${themeGrainVersion}.
Current Grain version: ${grainVersion}.""")
        }
    }

    /**
     * Gets File instance out of the relative path to file when running Grain in dev mode
     *
     * @param relativeFilePath relative path to file
     * @return File instance of the canonical file that corresponds to specified relative file path
     */
    private static File getFileInDevMode(String relativeFilePath) {
        def cl = ClassLoader.getSystemClassLoader() as URLClassLoader

        for (URL url : cl.getURLs()) {
            if (url.getFile().endsWith('/out/production/grain/')) {
                return new File(url.getFile().toString(), "../../../$relativeFilePath").canonicalFile
            } else if (url.getFile().endsWith('/build/classes/production/grain/')) {
                return new File(url.getFile().toString(), "../../../../$relativeFilePath").canonicalFile
            }
        }

        null
    }

    /**
     * Gets current class' classpath
     *
     * @return current class' classpath
     */
    private static String getCurrentClassPath() {
        def className = CmdlineParser.getSimpleName() + '.class'
        CmdlineParser.getResource(className).toString()
    }
}
