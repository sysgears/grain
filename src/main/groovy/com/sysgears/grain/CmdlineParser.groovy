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

package com.sysgears.grain

/**
 * Command line arguments parser and validator.
 * <p>
 * In case of facing wrong cmdline arguments dumps help and shutdowns the application 
 */
class CmdlineParser {

    /** Default Site config file name */
    private static final String CONFIG_FILE_NAME = 'SiteConfig.groovy'

    /** Default global config file name */
    private static final String GLOBAL_CONFIG_FILE_NAME = 'GlobalConfig.groovy'

    /**
     * Parses and validates command line arguments.
     * <p>
     * In case of detecting wrong cmdline arguments dumps help and shutdowns the application 
     *
     * @param args command line arguments 
     *
     * @return parsed command line options
     */
    public CmdlineOptions parse(String[] args) {
        def cli = new CliBuilder(usage: 'grain [command] [options]',
                header: '''Commands:
preview                   Run the site in preview mode on local web server
generate                  Generate site from source
deploy                    Deploy the static site
Options:
''')
        cli.with {
            e longOpt: 'env', args: 1, argName: 'environment', 'Override environment used for site config'
            v longOpt: 'verbose', args: 0, argName: 'verbose', 'Verbose output of file processing'
            h longOpt: 'help', 'Show usage information'
        }

        def configFile = new File(CONFIG_FILE_NAME)
        if (!configFile.exists() || !configFile.isFile()) {
            System.err.println("Unable to locate config file: ${configFile.canonicalPath}")
            cli.usage()
            System.exit(0)
        }

        def arguments = args.toList()

        def opts = new CmdlineOptions()
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

        def grainHomeProperty = System.getProperty('grain.home')
        if (grainHomeProperty) {
            opts.grainHome = new File(grainHomeProperty).canonicalFile
        } else {
            def cl = ClassLoader.getSystemClassLoader() as URLClassLoader

            for (URL url : cl.getURLs()) {
                if (url.getFile().endsWith('/out/production/grain/')) {
                    opts.grainHome = new File(url.getFile().toString() - '/out/production/grain/')
                } else if (url.getFile().endsWith('/build/libs/grain-1.0.jar')) {
                    opts.grainHome = new File(url.getFile().toString() - '/build/libs/grain-1.0.jar')
                }
            }

            if (!opts.grainHome)
                throw new RuntimeException('Unable to guess Grain home, please set grain.home system property')
        }
        opts.args = commands
        opts
    }
}
