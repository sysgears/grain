## 0.7.1 / 2017-04-20

  * Added support for CommonMark/Multimarkdown/Kramdown markdown processors emulated by Flexmark.
  * Marked pegdown markdown processor as deprecated. Added support for Flexmark emulation of pegdown.
  * Updated the Groovy version to 2.4.10.
  * Updated the Gradle wrapper version to 3.5.
  * Updated the Gpars version to 1.2.1.
  * Updated the Jetty version to 9.2.21.v20170120.
  * Updated the jRuby version to 9.1.8.0.
  * Updated the Jython version to 2.7.0.
  * Updated the setuptools version to 8.2.
  * Updated the Pygments version to 2.2.0.
  * Updated the Google Guice version to 4.1.0.
  * Updated the yuicompressor version to 2.4.8.
  * Updated the commons-io version to 2.5.
  * Updated the commons-cli version to 1.4.
  * Updated the Spock version to 1.0-groovy-2.4
  * Updated the snakeyaml version to 1.18
  * Updated the Logback version to 1.2.3 and jcl-over-slf4j to 1.7.25.
  * Disabled automatic generation of absolute links by the link tag if the resulting link is not a valid URL.


## 0.7.0 / 2016-09-19


  * Updated the Groovy version to 2.4.5.
  * Updated the Jetty version to 9.2.15.
  * Updated the Compass version to 1.0.3.
  * Switched to Java 7 NIO to watch file changes.
  * Removed some obsolete dependencies: jsoup, signpost, etc. to speed up dependency download.
  * Reworked how Grain finds the right RubyGems version for RMI Ruby. Now it tries to determine the gems
    version from system 'gem' command and, only if this fails, refers to the Ruby to RubyGems mapping. Updated
    the Ruby to RubyGems mapping to account newer Ruby versions.
  * Updated URL registry to automatically change a file extension for .md, .adoc and .rst files to .html,
    so these files can be properly rendered by a browser.
  * Updated how index pages can be accessed in preview mode. Now Grain returns the same page, either browser
    points to the directory or to the index page, so http://example.com/ and http://example.com/index.html
    will return the same page.
  * Added the *.swp* files created by the Vi text editor to the list of files excluded from processing. Excluded
    files are ignored by Grain when the site is generated.
  * Added the *.woff2* files to the list of binary files. Binary files are excluded from processing, but, contrary
    to the files from the excludes list, will be copied to the destination directory when the site is generated.
  * Fixed a rare, concurrency issue related to the resource header cache, when occasionally Grain failed to
    initialize the site preview mode due to unhandled concurrent access to the cache.
  * Suppressed the broken pipe exception produced by Jetty on page load. Jetty sometimes throws the broken pipe
    exception when, for example, a website page is quickly reloaded several times in preview mode. This is an
    ordinary behavior for Jetty which is aimed to prevent an application from generating and writing content
    that cannot be sent.
  * Modified the template engine to use iteration instead of recursion while generating Groovy script code in
    order to prevent stack overflows and log file path that Grain was unable to parse if any exception occurs.
  * Updated the URL registry to detect if the resources that are returned by the `resource_mapper` closure have
    identical URLs or do not have the `url` property defined.
  * Improved error handling for the Pygments highlighting to display the code snippet that caused the error as
    well as the language specified for the snippet.
  * Disabled the separate SASS installation. Compass automatically fetches the right SASS dependency.
  * Set the default encoding for Pygments lexers to UTF-8.

## 0.6.6 / 2015-08-27


  * Added support of dynamic revision numbers, like 0.6.+. Concrete values still must be specified for the major
  and minor numbers of the Grain version -- declarations like 0+, 0.+ or 0.6+ won't accepted. This is done on purpose
  to ensure that a theme always uses compatible Grain version
  * Fixed an issue when a value of the `script` header key was overwritten during parsing
  * Added defaults for `non_script_files` site configuration option, see
  <a href="http://sysgears.com/grain/docs/latest/#source-processing-configuration" target="_blank">source processing</a>

## 0.6.5 / 2015-08-04


  * Configured the Asciidoctor module to use custom document processing attributes from the site configuration
  * Updated the RPC executor to handle a map of string key-value pairs in order to pass Asciidoctor attributes

## 0.6.4 / 2015-06-22


  * Enabled Asciidoctor include support
  * Added exception dumping for the Asciidoctor

## 0.6.3 / 2015-01-20


  * Updated Asciidoctor version to 1.5.2
  * Enabled syntax highlighting in Asciidoctor using Coderay

## 0.6.2 / 2014-10-09


  * Added 'none' option for the compass setting to disable compass processing
  * Fixed the script parsing issue, when Groovy expressions could be escaped incorrectly for script files that
    exceed 30k characters
  * Added font files to the list of default binary files

## 0.6.1 / 2014-03-14


  * Implemented the 'clean' command which allows to remove project temporary directories

## 0.6.0 / 2014-02-17


   * Reworked strategy of getting Python and Ruby dependencies. Python packages and Ruby gems are downloaded and used
     by Grain from Python package index and RubyGems repositories respectively. If there is Python interpreter
     installed in the system, it will be used to execute downloaded packages, if not, Grain will fall back to Jython.
     The same approach is used for Ruby/JRuby
   * Added reStructuredText markup support via Python docutils
   * Added AsciiDoc markup support via official Ruby gem
   * Dynamic rendering implemented, it enables to compose and render pages in memory
   * PegDown Markdown processor is used for markdown processing
   * Made Python and Ruby interpreters executables, that are used in system, configurable via configuration file
   * Added default configuration for the most vital configuration values
   * Implemented persistent caching for results of reStructuredText and AsciiDoc markup rendering
   * The order in which Grain components participate in resource rendering is changed. Embedded Groovy code is executed
     first and then the markup is processed.
   * Added ability to preserve text blocks from being processed by markup rendering processors using special
     syntax
   * Added ability to override page layout in resource mapper
   * Introduced configuration property that allows to set which files to exclude from being processed by Grain
   * Fixed bugs related to `r` and `rs` tags not generating proper url for cases when site isn't deployed to the root
     folder
   * Added `link` tag to default taglib. It allows to specify proper link out of a path, relative to site's base URL
   * Fixed bug related to unexpected behaviour when illegal number of arguments is specified for the custom
     command-line commands

## 0.5.2 / 2014-01-28


  * Fixed bug with missing </p> tags right below highlighted code excerpts

## 0.5.1 / 2014-01-13


  * Added possibility to execute closures as deploy commands

## 0.5.0 / 2013-12-24


  * First public release