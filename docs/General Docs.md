# Grain Project Overview

The Grain framework is an open source static website generator written Groovy, and it provides an extensible mechanism
for implementing custom static websites or generating software documentation.

If you want to get started with Grain, check out the `README.md` file in the project root:

* [Getting Started]

## Table of Contents

* [IDE Integration](#ide-integration)
    * [IntelliJ IDEA](#intellij-idea)
    * [Eclipse](#eclipse)
    * [NetBeans IDE](#netbeans)
* [Grain website structure](#grain-website-structure)
    * [Naming convention for directories](#naming-conventions-for-directories)
    * [Grain configuration](#grain-configuration)
        * [Interpreters configuration](#interpreters-configuration)
        * [Python interpreter](#python-interpreter)
        * [Ruby interpreter](#ruby-interpreter)
        * [Deployment configuration](#deployment-configuration)
* [Environments](#environments)
* [Advanced Grain configuration](#advanced-grain-configuration)
* [Page structure](#page-structure)
	* [Page file source](#page-file-source)
	* [Embedded Groovy code](#embedded-groovy-code)
* [Layouts](#layouts)
	* [Concept](#concept)
	* [Layout nesting](#layout-nesting)
* [Includes](#includes)
	* [General idea](#general-idea)
	* [Passing custom model](#passing-custom-model)
* [URL and resource mapping](#url-and-resource-mapping)
	* [Introduction](#introduction)
	* [Resource representation](#resource-representation)
	* [Default Resource URL](#default-resource-url)
	* [Resource processing phases](#resource-processing-phases)
	* [Mapping customizations](#mapping-customizations)
	* [Dynamic rendering](#dynamic-rendering)
* [Tag libraries](#tag-libraries)
	* [Standard tag library](#standard-tag-library)

## IDE Integration

### IntelliJ IDEA

1. Import your project by selecting from menu: `File -> Import Project -> Select your website dir -> Gradle`
2. Right-click on the `theme/src/com.example.grain/Launcher` file and select `Run` to launch Grain in preview mode.

### Eclipse

#### Prerequisites:

1. Install the [Groovy-Eclipse] plugin and Groovy Compiler 2.1.

#### Importing project:

1. Generate Eclipse project files by executing `./gradlew eclipse` or `gradlew.bat eclipse` in the website directory.
2. Import your project by selecting from Eclipse menu:

`File -> Import... -> Existing Projects Into Workspace -> Select your website dir`

3. Right-click on the `theme/src/com.example.grain/Launcher` file and select `Run As -> Java Application` to launch
Grain in preview mode.

### NetBeans

#### Prerequisites:

1. Install Groovy and Grails plugin by selecting from the NetBeans menu:

`Tools -> Plugins -> Available Plugins -> Groovy And Grails -> Install`

#### Importing a project:

1. Generate the Eclipse project files by executing `./gradlew eclipse` or `gradlew.bat eclipse` in the website
directory.
2. Import your Eclipse project by selecting from menu:

`File -> Import Project -> Eclipse Project -> Import Project ignoring project dependencies`.

3. Select your website directories in both fields.

4. Right-click on the `theme/src/com.example.grain/Launcher` file and select `Run File` to launch Grain in preview mode.

## Grain website structure

### Naming convention for directories

Grain has the following conventions for creating website files and directories:

- `content`, a directory with the website content, organized the same way as in the resulting website
- `theme`, a top-level directory for the Grain theme that organized content
    - `includes`, a directory with the common web page parts &mdash; header or footer &mdash; included in the layouts
    - `layouts`, a directory with the web page layouts (templates)
    - `src`, a directory with Groovy classes used for processing the website content
    - ... - other assets, organized the same way as in resulting website
- `SiteConfig.groovy` - website configuration parameters
  
## Grain configuration

Grain provides the `SiteConfig.groovy` file for general configurations. This file is located in the root folder of a
Grain theme. The configuration settings are specified with the [ConfigSlurper] syntax.

### Predefined variables

When working with `SiteConfig.groovy`, you may use a couple of pre-defined variables:

* `site`, this is an access point to website resources and configuration.
* `log`, this is a default logger

### Filesystem layout

Though in many situations it's best to follow the default conventions, it may still be beneficial to have a fine-grained
control over the Grain website structure.

> The default Grain configurations can be found in the [`src/main/resources/DefaultConfig.groovy`] file.

You can control the website filesystem layout by modifying the following parameters in the `/SiteConfig.groovy`:

* `destination_dir`, the destination directory for generated website files. Defaults to:

```groovy:nl
destination_dir = "${base_dir}/target"
```

* `cache_dir`, the directory for storing cached files of various Grain subsystems. Defaults to:

```groovy:nl
cache_dir = "${base_dir}/.cache"
```

* `source_dir`, the directory or a list of directories with website source files. The directories are handled
sequentially by Grain, and if the same files with same relative locations appear in each directory, then the file from
the directory _listed first_ takes precedence. Defaults to:

``` groovy:nl
source_dir = ["${base_dir}/content", "${base_dir}/theme", "${cache_dir}/compass"]
```

* `include_dir`, the directory or a list of directories with includes. Defaults to:

```groovy:nl
include_dir = ["${theme_dir}/includes"]
```

* `layout_dir`, the directory or a list of directories with website layouts. Defaults to:

```groovy:nl
layout_dir = ["${theme_dir}/layouts"]
```

#### Customizing the filesystem layout

A custom destination and cache folders can be specified as follows:

``` groovy:nl
destination_dir = "${base_dir}/site"

cache_dir = "${base_dir}/.site_cache"
```

To redefine the source, include, or layout folders, you should provide a list of directories, or, if you want to keep
the default settings, add your directories to the existing list from the default Grain configuration:

``` groovy:nl
// adding a directory to the predefined list
source_dir << "${base_dir}/assets"

// redefining the folders altogether:
source_dir = ["${base_dir}/content", "${base_dir}/theme", "${base_dir}/assets"]
```

### Source processing configuration

These settings can be used for excluding files or directories located in the source folders, or for defining assets that
must be copied to the destination folder without additional processing.

* `excludes`, a list of regular expressions that match the locations of files or directories that must be completely
excluded from processing. These files are ignored by Grain and won't be copied to the destination directory. Defaults
to:

``` groovy:nl
excludes = ['/sass/.*', '/src/.*', '/target/.*']
```
             
* `binary_files`, a list of regular expressions that match the locations of binary files. The binary files are excluded
from processing, but, contrary to the files from the excludes list, they'll be copied to the destination directory.
Defaults to:

``` groovy:nl
binary_files = [/(?i).*\.(png|jpg|jpeg|gif|ico|bmp|swf ... eot|otf|ttf|woff|woff2)$/]
```

* `non_script_files`, a list of regular expressions that match the locations of files with the content
(see [file source](#page-file-source)) that must not be processed. The file headers are still parsed, which is useful
when you need to pass some configuration options, but don't want Grain to run embedded Groovy code. Defaults to:

``` groovy:nl
non_script_files = [/(?i).*\.(js|css)$/]
```

#### Customizing source processing settings

It's recommended that you add new regular expressions to the default processing configuration and keep the default
settings. However, you can completely redefine the configuration if required:

``` groovy:nl
excludes << '/misc/.*' // additionally excludes the 'misc' directory

excludes = ['/src/.*', '/target/.*'] // overwrites the default configuration
```

### Preview configuration

* `jetty_port`, sets a TCP port for serving HTTP request in preview mode. Defaults to `4000`.

### Features configuration

Grain has many features provided by different implementations. Concrete implementations are specified in the Features
section of the configuration file.

#### Markdown markup feature

`markdown` - markdown markup implementation (default: txtmark)
  
  - `txtmark` - TxtMark Markdown 
  - `pegdown` - PegDown Markdown (Since this markdown processor has been [deprecated](https://github.com/sirthias/pegdown) by developers, please consider using the `flexmark_pegdown` in case you need to use pegdown for your pages.)
  - `flexmark_pegdown` PegDown implementation, provided by [Flexmark](https://github.com/vsch/flexmark-java)
  - `flexmark_kramdown` Kramdown Markdown
  - `flexmark_commonmark` CommonMark Markdown (this option is the default one for [Flexmark](https://github.com/vsch/flexmark-java))
  - `flexmark_multimarkdown` Multimarkdown Markdown

All options with names starting with "flexmark_" prefix provide markdown processors which are emulated by [Flexmark](https://github.com/vsch/flexmark-java).
  
#### reStructuredText markup feature

There's only one implementation of reStructuredText that uses Python docutils. All files having the `rst` extension are
rendered using this implementation.

#### Asciidoctor markup feature

All files having the `adoc` or `asciidoctor` extensions will be rendered using the latest `asciidoc` Ruby gem.

The files are converted to HTML5 with the help of the `Asciidoctor.convert()` method:

```ruby:nl
Asciidoctor.convert(source, :safe => 0, :attributes => attributes)
```

You can provide the custom attributes for the document conversion in the following way:

```groovy:nl
features {
    asciidoc {
        opts = ['source-highlighter': 'coderay',
                'icons': 'font']
    }
}
```

#### Syntax highlighting feature

`highlight` - code highlighting method (default: none):

  - `none` - no code highlighting
  - `pygments` - code highlighting using Pygments   

`cache_highlight` - cache highlighting results (default: true):

  - `true` - cache highlighting results
  - `false` - do not cache highlighting results

#### SASS/Compass feature

Grain supports Sass and SCSS stylesheets. The support is done by launching an external Compass process that watches for
Sass and SCSS files and re-compiles them automatically. The settings for Compass should be stored in the `/config.rb`
file, which is rendered to `cache_dir/config.rb` as an ordinary page and can use the website configuration parameters
and have embedded Groovy code. The Compass process is launched in the `cache_dir` directory and reads settings from
`config.rb`.

#### Minification and compression features

The generated website files can be minified and compressed in various ways.

**Minification**
 
`minify_xml` - minify XML files (default: `none`)

  - `none` - do not minify XML files
  - `true` - minify XML files 

`minify_html` - minify HTML pages (default: `none`)

  - `none` - do not minify HTML files
  - `true` - minify HTML files 

`minify_js` - minify JavaScript files (default: `none`)

  - `none` - do not minify JavaScript files
  - `true` - minify JavaScript files 

`minify_css` - minify CSS stylesheets (default: `none`)

  - `none` - do not minify CSS files
  - `true` - minify CSS files 

**Compression**

`compress` - compress all generated files (default: `none`)

  - `none` - do not compress generated files
  - `gzip` - compress all generated files using GZIP

### Interpreters configuration

#### Python interpreter

Grain uses the Python interpreter in your system to run various Python packages. If Python interpreter isn't found,
Grain falls back to Jython. You can specify an ordered list of Python command candidates in configurations:

* `python.cmd_candidates`, Python command candidates. Defaults to `['python2', "python", "python2.7"]`.

It's possible to force Grain to use Jython by setting the `python.interpreter` property to `jython`:

* `python.interpreter`, a Python interpreter. Defaults to `auto`:
    - `auto` - use Python if it's installed; otherwise fall back to Jython
    - `python` - use Python (requires Python installed)
    - `jython` - use Jython

Grain uses *setuptools* to manage Python packages. If required, you can change the *setuptools* version in the following
option:

* `python.setup_tools`, Python *setuptools* version. Defaults to `2.1`.

#### Ruby interpreter

Grain tries to find the Ruby interpreter in your system to run various Ruby gems. If the Ruby interpreter isn't found,
Grain falls back to JRuby. You can specify an ordered list of Ruby command candidates in the configuration:

* `ruby.cmd_candidates`, Ruby command candidates. Defaults to
`["ruby", "ruby1.8.7", "ruby1.9.3", "user.home/.rvm/bin/ruby"]`.

It's possible to force Grain to use JRuby by setting the `ruby.interpreter` property to `jruby`:

* `ruby.interpreter`, sets the Ruby interpreter. Defaults to `auto`. Possible options:
    - `auto` - use Ruby if it's installed in the system; otherwise fall back to JRuby
    - `ruby` - use Ruby (requires Ruby installed)
    - `jruby` - use JRuby

You can change the *RubyGems* package manager version in the following option:

* `ruby.ruby_gems`, sets the *RubyGems* version. The default value depends on the currently used Ruby version.

### Deployment configuration

Deployment of the final Grain-based website is implemented as a shell command execution.

* `deploy`, a shell command or list of shell commands to deploy a website, that are executed sequentially.
 
**Example**

```groovy:nl
s3_bucket = "www.example.com"
deploy = "s3cmd sync --acl-public --reduced-redundancy ${destination_dir}/ s3://${s3_bucket}/"
```

### Environments

Different modes of Grain operation are associated with different configuration environments. These environments can be
used to provide environment specific configuration.

**Example**

```groovy:nl SiteConfig.groovy
environments {
    dev {
        log.info "Development environment is used"
        // Preview-specific configuration
    }
    prod {
        log.info "Production environment is used"
        // Generate-specific configuration
    }
}
```

Grain uses the following environments:

* `dev` - the environment for running a Grain website in preview and development modes

* `prod` - the environment for generating and deploying the website

* `cmd` - a theme-specific command-mode environment for running a custom command defined in `SiteConfig.groovy`
   
### Advanced Grain configuration

#### `config_posthandler` hook

* `config_posthandler`,  a closure executed right after each execution of `SiteConfig.groovy`.
 
**Example**

```groovy:nl
config_posthandler = { println 'Config has been just rereaded' }
```

## Page structure

### Page file source

A typical Grain page consists of the page header and page content. The page header contains the static page
configuration parameters formatted using YAML.

```yaml:nl
---
layout: page
title: "Page title"
...
---
Page content goes here
```

The page content can be written in Markdown, HTML, XML, or plain text. Grain determines the content markup type based
on the file extension.

### Embedded Groovy code

Embedded Groovy code can be included anywhere in the page content except the header. To include a simple Groovy
expression, you can use these notations:

```groovy:nl
${2 + 2}
```

Or:

``` groovy:nl
<%= 2 + 2 %>
```

In both cases, the value `4` is inserted into the page content as the result of evaluation.

To include large blocks of Groovy code, use the notation below:

``` jsp:nl
<%
    def foo = 2 + 2
%>
...
<% if (foo == 4) { %>
   <span class="true"></span>
<% } %>
``` 

The `if` above will work as expected, e.g., a `span` tag will be rendered into the page only when the expression is
evaluated to `true`. Also note that variables declared in one piece of embedded code will be available everywhere on the
page.

#### Disabling Groovy code interpolation

To render embedded Groovy code _as is_, you need to disable Groovy code interpolation by using the following form of
escaping:

```
`!`${2 + 2}`!`
```

### Variables on a page

Grain has several variables reserved, others can be freely introduced on a page.

The following variables are reserved in Grain:

  - `page` - the current page model
  - `site` - a facade for accessing website resources and configurations
  - `content` - the HTML content of the page, accessible in layout
  - `out` - an internal string buffer necessary to accumulate the content during rendering

#### Page variable

The `page` variable provides access to all the keys of YAML headers in pages.

For example, to get the page title, you can use the following code snippet:

``` jsp:nl
${page.title}
```

Grain generates the following header keys on initial loading of a resource from the source file:

  - `location` - a relative page location on the filesystem, for example, `/index.html` or `/contacts/index.markdown`
  - `url` - URL of the page
  - `type` - a resource type, either `page` for resources that render into HTML or `asset` for all the other resources
  - `dateCreated` - the resource file creation time in milliseconds
  - `lastUpdated` - the resource file last update time in milliseconds
  - `text` - the text contents of the resource file
  - `bytes` - the byte contents of the resource file
  - `script` - indicates whether processing of the embedded Groovy code is enabled for the file. Defaults to `false` if
  a file location matches an expression from the `non_script_files` configuration list. Defaults to `true` otherwise.

The `page` variable is a Groovy map. You can add keys to this map in the page code or make other changes, but these
changes are only visible to the page itself, they aren't visible on other pages.

#### Site variable

The `site` variable provides access to all the properties declared in `SiteConfig.groovy`. For example, in order to get
a configured URL of the website, you can use this code:

``` jsp:nl
<a href="${site.url}">Home</a>
```

`site` also exposes all the website pages in the `site.pages` property. All the website assets are set in the
`site.assets`, and both assets and pages are set in `site.resources`. For example, to dump the value of title defined in
every website page, you can run use the following code:

``` jsp:nl
<%= site.pages.collect { it.title } %>
```

### Disabling code processing

You can define whether to process the page content using the `non_script_files` configuration:

``` groovy:nl
non_script_files = [/(?i).*\.(js|css)$/]
```

You can also overwrite the configuration for a single file by changing the value of the `script` key in the header:

``` yaml:nl
---
script: true # true - evaluate embedded Groovy expressions, false - render the page content as is
#...
---
```

This usually comes in handy when you need to pass variables to stylesheet or JavaScript files.

## Layouts

### Concept

Rendered page content usually wrapped up by layout, where most of the presentation logic is held.

Here is an example of a layout:

``` jsp /theme/layout/default.html
<html>
<head>
  <meta charset="utf-8">
  <title>${site.title}</title>
</head>

<body>
  <div id="main">
    <div id="content">
      ${content}
    </div>
  </div>
</body>
</html>
```

Please note that rendered page contents is passed in `content` variable to the layout.

### Layout nesting

One layout can be based on another layout. For example, here is some page layout, based on default layout above:

``` jsp /theme/layout/page.html
---
layout: default
title: "Default page title"
---
<div class="page">
    ${content}
</div>
```

Layout nesting can be unlimited.

## Includes

### General idea

Common page presentation parts can be kept in separate files and then included into layouts.

For example in the code below sidebar.html is included into layout:

``` jsp /theme/layout/page.html
---
layout: default
title: "Default page title"
---
...
${include 'sidebar.html'}
```

### Passing custom model

In some cases you would want to pass some variables to the included parts:

``` jsp /theme/layout/blog.html
---
layout: page
---
...
           <footer>
             <a href="${page.url}">Read More</a>
             ${include 'tags.html', [tags: page.categories]}
           </footer>
```

After that, inside tags.html, you will have `page.tags` set to the value of `page.categories`.

## URL and resource mapping

### Introduction

It's obvious that to implement complex tasks for your website, you may need to have full control over your website
resources. For example, you may want to customize website URLs, add custom pages, and so on.

These transformations can be done by using the resource mapping mechanism. To use this mechanism, it's important to be
familiar with how the website resources are represented in Grain.

### Resource representation

In Grain each resource is represented as a plain Groovy map. Each resource should have at least three keys in its map:

- `location`, which points to the resource file in filesystem, or `source`, which contains resource content
- `markup` - resource markup, one of `'html'`, `'md'`, `'rst'`, `'adoc'`, `'text'`, or `'binary'` (it's optional if
`location` is defined)
- `url` - the URL of the resource (it's not strictly tied to the resource location and can be modified)

Both `source` and `markup` have higher priority over `location` when Grain decides what content should be rendered and
what markup should be used for rendering.

Along with these keys, resource representation holds all the properties specified in content files' headers.

### Default Resource URL

Grain assigns a default resource URL based on the resource location. For example: if the favicon.ico is placed
in the /images/icons/ folder, the icon's default url will be the /images/icons/favicon.ico.

Additionally, Grain rewrites a file extension for `.md`, `.adoc` and `.rst` files, so they can be rendered by a browser:

``` :nl
file location    -> file url

/test.adoc       -> /test.html
/test.markdown   -> /test.html
/test/test.adoc  -> /test/test.html
/test/test.rst   -> /test/test.html
```

    ### Resource processing phases

Grain processes all the resources located in the source directories in the following way:

 - scans all the `source_dirs` listed in the `SiteConfig.groovy`, and loads the raw list of resources
 - parses headers of all the found non-binary resources, and then puts the headers as well as other important
information, like resource `location`, `url`, and `markup`, to the resource representation map
 - passes all the found resources to the ResourceMapper closure. You can use this closure (described in the next section)
to modify the list of resources in any way: add new pages or assets, change their URLs, update content and headers, remove
pages, etc. The outcome of this phase is a modified list of resources, which may contain new assets and pages.
 - adds all the resources returned by the resource mapper to the url registry and generates the pages by resolving
layouts and executing tag libraries.

### Mapping customization

As mentioned above, the list of all the site resources is passed to the `SiteConfig.groovy -> resource_mapper` closure.
The closure is expected to build new list of resources that may have customized URLs, modified resource variables, etc.

For example the input list of resources might look like this:

``` groovy:nl
[
    [location: '/articles/my-new-post-title.markdown', url: '/articles/my-new-post-title/'],

    [location: '/blog/index.html', url: '/blog/']
    ...
]
```

And the output could be as follows:

``` groovy:nl
[
    [location: '/articles/post-sample-with-code.markdown',
        url: '/blog/2013/10/21/post-sample-with-code/'],

    [location: '/blog/index.html', url: '/blog/'],

    [location: '/blog/index.html', url: '/blog/page/2/',
        posts: [location: '/articles/post-sample-with-code.markdown', ...]],

    [location: '/blog/index.html', url: '/blog/page/3/'
        posts: [...]]
]
```

Note how one physical resource `/blog/index.html` is mapped here to different URLs and each time `/blog/index.html` will
receive additional model variable `posts`.

#### Resource mapper closure

`resource_mapper` - a closure that is executed each time website changes for transforming initial resource models

***Parameters***:

  1. List of initial resource models

***Return value***: transformed resource models to be used for actual rendering of resources

***Example***

``` groovy:nl
resource_mapper = { resources ->
    resources.collect { Map resource ->
        if (resource.location =~/\/blog\/.*/) {
            // Select all the resources, which content files placed under /blog dir
            // Rewrite url for blog posts
            def unParsedDate = resource.date //Date specified in content file's header
            def date = Date.parse(site.datetime_format, resource.date).format('yyyy/MM/dd/')
            def title = resource.title.toLowerCase().replaceAll("\\s+","-")
            resource + [url: "/blog/$date$title/"]
        } else {
            resource
        }
    }
}
```

### Dynamic rendering

Resource in Grain can be rendered dynamically anywhere, to do this one should assemble resource map and call
method render() on it.

Example:

``` groovy:nl
[source: '**Bold text**', markup: 'adoc'].render()
```

Example of using dynamic rendering inside page:

``` jsp:nl
<div>
    ${[source: '**Bold text**', markup: 'adoc'].render()}
</div>
```

## Tag libraries

### Standard tag library

Grain provides standard tags for most vital tasks, additional tags are recommended to be added to custom theme tag lib.

The standard tags are:

 - **`r`** - looks up resource URL by resource location. Finds CDN url of the resource if `cdn_urls` property is defined
 in the `SiteConfig.groovy`, or, alternatively, generates the resource url using the `link` tag.

    ###### Parameters:

       1. Resource location
 
    ###### Example:

    ``` jsp:nl
    <link href="${r '/favicon.png'}" rel="icon">
    ```

  - **`link`** - generates an url from a relative link to a resource (for example, */path/to/resource.ext*).

  This tag allows you to insert absolute or relative links depending on a value of the `site.generate_absolute_links` 
  boolean variable. If `site.generate_absolute_links` variable is set to **true**, then Grain just concatenates the 
  `site.url` property (*http://domain.com/your-app*) and a resource link (for example, setting the `'.'` value as a 
  parameter of the `site.url` will result in creating an absolute path - *./path/to/resource.ext*). And if 
  `site.generate_absolute_links` variable is set to **false**, or is not set at all, then Grain tries to extract a 
  path from the `site.url` and prepend it to the resource link (*/your-app/path/to/resource.ext*). 
 
    ###### Parameters:

       1. Relative link
  
    ###### Example:

    ``` jsp:nl
    <link href="${link '/blog/post'}">
    ```

 - **`rs`** - looks up multiple resource URLs by their locations

    ###### Parameters:

      1. Resource location list

    ###### Example:

    ``` jsp:nl
    <% rs(['/javascripts/libs/jquery.min.js',
    '/javascripts/modernizr-2.0.js',
    '/javascripts/octopress.js',
    '/javascripts/github.js',
    '/javascripts/jquery.tweet.js',
    '/javascripts/twitter-options.js']).each { script -> %>
    <script src="${script}" type="text/javascript"></script><% } %>
    ```

 - **`include`** - inserts rendered resource contents

    ###### Parameters:

      1. Template location
      1. *(Optional)* Additional model variables added to `page` map

    ###### Example:

    ``` jsp:nl
    ${include 'tags.html', [tags: post.categories]}
    ```

 - **`md5`** - calculates md5 hash of a byte array

    ###### Parameters:

      1. Byte array

    ###### Example:

    ``` jsp:nl
    md5(resource.render().bytes)
    ```

### Custom tag libraries

You can add your own tags in your website theme. This can be made by implementing your tags as Groovy closures.

Class with tag closures should be added into the list of tag libs in `SiteConfig.groovy -> tag_libs`.
The tag lib class constructor should expect one argument - standard Grain taglib.
 
Example:

``` groovy:nl SiteConfig.groovy
import com.example.grain.taglib.MyTagLib
...
tag_libs = [OctopressTagLib, MyTagLib]
```

``` groovy:nl /theme/src/com/example/grain/taglib/MyTagLib.groovy
package com.example.grain.taglib

import com.sysgears.grain.taglib.GrainTagLib

class MyTagLib {

    /**
     * Grain taglib reference.
     */
    private GrainTagLib taglib

    public MyTagLib(GrainTagLib taglib) {
        this.taglib = taglib
    }

    /**
     * Renders a brief from the markdown post, delimited by <!--more--> 
     *
     * @attr markdown post content
     */
    def renderBrief = { String markdown ->
        Processor.process(markdown.split('<!--more-->').head())
    }

    ...

}
```

## Custom command-line commands

### Creating your own commands

It can be beneficial to add support for new custom commands to Grain command-line for pre-populating new pages, posts,
etc.

All the commands supported by theme should be stored into `commands` list property in `SiteConfig.groovy`. Each command
in this list is a closure that accepts zero or more String parameters that is passed from command-line.

***Example***

``` groovy:nl
commands = [
new_post: { String postTitle ->
    def date = new Date()
    def fileDate = date.format("yyyy-MM-dd")
    def filename = fileDate + "-" + postTitle.toLowerCase().replaceAll("\\s+","-") + ".markdown"
    def file = new File(content_dir + "/blog/" + filename)
    file.exists() || file.write("""---
layout: post
title: "${postTitle}"
---
""")},
...
]
```

[getting started]: https://github.com/sysgears/grain#getting-started
[configslurper]: http://docs.groovy-lang.org/latest/html/gapi/groovy/util/ConfigSlurper.html
[groovy-eclipse]: http://groovy.codehaus.org/Eclipse+Plugin
[src/main/resources/DefaultConfig.groovy]: https://github.com/sysgears/grain/blob/master/src/main/resources/DefaultConfig.groovy