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

import java.util.jar.Attributes
import java.util.jar.Manifest

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
        
        opts.vendorHome = getVendorHome()

        opts.args = commands
        opts
    }

    /**
     * Detects vendor home directory 
     * 
     * @return Grain vendor home directory
     */
    private static File getVendorHome() {
        def vendorHome = null

        def vendorHomeProperty = System.getProperty('vendor.home')
        if (vendorHomeProperty) {
            vendorHome = new File(vendorHomeProperty).canonicalFile
        } else {
            def className = CmdlineParser.getSimpleName() + ".class"
            String classPath = CmdlineParser.getResource(className).toString()
            
            if (!classPath.startsWith("jar")) {
                def cl = ClassLoader.getSystemClassLoader() as URLClassLoader

                for (URL url : cl.getURLs()) {
                    if (url.getFile().endsWith('/out/production/grain/')) {
                        vendorHome = new File(url.getFile().toString(), "../../../vendor").canonicalFile
                    }
                }

                if (!vendorHome) {
                    throw new RuntimeException('Unable to guess Grain vendor home, please set vendor.home system property')
                }
            } else {
                def manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) +
                        "/META-INF/MANIFEST.MF"
                def manifest = new Manifest(new URL(manifestPath).openStream())
                def attr = manifest.getMainAttributes()
                def rev = attr.getValue("Built-Rev")
                
                vendorHome = new File(System.getProperty('user.home'), ".grain/vendor/${rev}")
            }
        }
        
        vendorHome
    }
}
