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