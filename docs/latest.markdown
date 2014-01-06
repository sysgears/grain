##Overview

###Description

Grain framework is an open source static website generator for Groovy that provides extensible mechanisms for
implementing custom static sites or generating software documentation.

####Grain features

Grain comes with the following high-level features:

 - preview mode that allows to make and see changes on the fly
 - support of embedded Groovy code for any content files
 - conventions that allow to process content sources using Groovy code
 - support of Markdown and reStructuredText
 - support of SASS/SCSS
 - code highlighting via Pygments
 - sources compression and minification

####Grain themes

Grain website project is called *theme*. Theme defines site structure, appearance and content arrangement.
Typically, Grain theme ships with:

 - website and Grain features configuration
 - HTML page templates, called *layouts*
 - stylesheets, javascripts and images
 - code sources to process site pages (for instance, apply pagination)
 - samples of raw content files

Grain has ready-made themes that can be found [here](http://sysgears.com/grain/themes/).
It is not required to know Groovy or any other programming language to use these themes, you still can easily
manage content and set up your site.

###Requirements

To run Grain framework you need [JDK 6 or later](http://www.oracle.com/technetwork/java/javase/downloads/index.html).
Download and install the appropriate JDK for your operating system.

###Installation

No installation required. Just download one of the [themes](http://sysgears.com/grain/themes/) and Grain will be
loaded automatically as a JAR dependency.

If you are new to Grain, we recommend to start with ready-made Grain [Octopress](http://sysgears.com/grain/themes/octopress/) theme,
it would give you a good overview on how to efficiently use Grain features.
To build website from scratch, download Grain theme [template](http://sysgears.com/grain/themes/template/).

##Getting Started

###Create website

In order to start creating your website, download one of the [themes](http://sysgears.com/grain/themes/) and unpack it
to the location of your choice.

###Preview website
Navigate to the location of your newly created website `cd /path/to/your_site` and run the command
``` sh:nl
./grainw
```

to launch your website in preview mode.

> Here and further command-line snippets work for Unix-like systems only. In case you are running Grain from
Windows simply use `grainw` command instead of `./grainw`.

After that you can view your website by pointing a web browser to `http://localhost:4000`, then you can add/change/delete
files of your website and see all changes in the web browser immediately after refreshing the page.

###Generate and deploy
When your site is ready for going live you can generate all the website files by executing
``` sh:nl
./grainw generate
```

The files of website will be generated to `/path/to/your_site/target` directory. 

You can deploy the resulting files either manually or with the help of Grain:
``` sh:nl
./grainw deploy
```
Check the deployment section for more information.

##IDE Integration

###IntelliJ IDEA

  1. Import project by selecting from menu: `File -> Import Project -> Select your website dir -> Gradle`
  1. Right click on theme/src/com.example.grain/Launcher and select `Run` to launch Grain in preview mode.

###Eclipse

####Prerequisites:

  1. Install [Groovy-Eclipse plugin](http://groovy.codehaus.org/Eclipse+Plugin) along with Groovy Compiler 2.1

####Importing project:

  1. Generate Eclipse project files by executing `./gradlew eclipse` or `gradlew.bat eclipse` in website directory.
  1. Import project by selecting from menu: `File -> Import... -> Existing Projects Into Workspace -> Select your website dir`
  1. Right click on theme/src/com.example.grain/Launcher and select `Run As -> Java Application` to launch Grain in preview mode.

###NetBeans IDE

####Prerequisites:

  1. Install Groovy And Grails plugin by selecting from the menu: `Tools -> Plugins -> Available Plugins -> Groovy And Grails -> Install`

####Importing project:

  1. Generate Eclipse project files by executing `./gradlew eclipse` or `gradlew.bat eclipse` in website directory.
  1. Import Eclipse project by selecting from menu: `File -> Import Project -> Eclipse Project -> Import Project ignoring project dependencies`.
     Select your website dir in both fields.
  1. Right click on theme/src/com.example.grain/Launcher and select `Run File` to launch Grain in preview mode.

##Website structure

###Directories conventions

Grain has the following conventions for website files and directories: 

  - content - website content, organized the same way as in resulting website
  - theme - top level directory for the site content representation theme
    - includes - common page parts, included at the layouts side, such as header, footer, etc.
    - layouts - page layouts
    - src - any Groovy classes used for processing content
    - ... - other assets, organized the same way as in resulting website
  - SiteConfig.groovy - website configuration parameters
  
##Configuration

###Predefined variables

Grain provides `SiteConfig.groovy` file for general configuration. For specifying configuration in this file, use
[ConfigSlurper](http://groovy.codehaus.org/ConfigSlurper) syntax. When working with `SiteConfig.groovy`, you
may use set of pre-defined variables. These variables are:

`site` - access point to website resources and configuration

`log` - default logger

###Filesystem layout

Though in many situations you wouldn't touch default conventions, in some cases it is still beneficial
to have the possibility for more fine-grained control over Grain website structure on the filesystem.

You can control website filesystem layout by modifying the following parameters in `SiteConfig.Groovy`:

`cache_dir` - directory where cache files of various Grain subsystems are stored

`source_dir` - directory or list of directories with website sources. The directories are handled sequentially
by Grain and if the same files with same relative locations appear in each directory, then the file from the directory
listed later takes precedence

`include_dir` - directory or list of directories with includes

`layout_dir` - directory or list of directories with layouts

`destination_dir` - destination directory for generated website files

`excludes` - a list of regular expressions, that match locations of files or directories to be excluded
             from processing by Grain

###Preview configuration

`jetty_port` - TCP port for serving HTTP request in preview mode (default is `4000`)

###Features configuration

Grain has many features provided by different implementations. Concrete implementations are specified in the `features`
section of the configuration file.

####Markdown markup feature

`markdown` - markdown markup implementation (default is `txtmark`)
  
  - `txtmark` - TxtMark Markdown 
  - `pegdown` - PegDown Markdown

####reStructuredText markup feature

`rst` - reStructuredText implementation (default is `jrst`)
  
  - `jrst` - JRst

####Syntax highlighting feature

`highlight` - code highlighting method (default is `none`):

  - `none` - no code highlighting
  - `pygments` - code highlighting using Pygments   

`pygments` - Pygments integration method (default is `auto`):

  - `none` - do not use Pygments 
  - `python` - use Pygments bundled with Grain as Python process (requires Python installed)
  - `jython` - use Pygments bundled with Grain as Jython process (slow startup time)
  - `shell` - use Pygments installed in the system as shell Python process
  - `auto` - use Pygments bundled with Grain as Python process if Python is installed in the system,
             otherwise fallback to Jython process 
 
`cache_highlight` - cache highlighting results (default is `true`):

  - `true` - cache highlighting results
  - `false` - do not cache highlighting results
  
####SASS/Compass feature

Grain supports defining stylesheets using SASS or SCSS files. This is done by launching external Compass process
which watches for SASS/SCSS files and recompiles them automatically. The settings for Compass should be stored
in `/config.rb` file, which is rendered to `cache_dir/config.rb` as an ordinary page and hence
can use website configuration parameters and have embedded Groovy code. The Compass process is launched in the 
`cache_dir` directory and reads settings from `config.rb`. 

`compass`- SASS/Compass integration method (default is `none`):

  - `none` - do not launch Compass 
  - `ruby` - use Compass bundled with Grain as Ruby process (requires Ruby installed)
  - `jruby` - use Compass bundled with Grain as JRuby process (slow startup time, generally slower than Ruby process)   
  - `shell` - use Compass installed in the system as shell Ruby process
  - `auto` - use Compass bundled with Grain as Ruby process if Ruby is installed in the system,
             otherwise fallback to JRuby process
             
####Minification and compression features

The generated files of website can be minified and compressed in various ways.

#####Minification
 
`minify_xml` - minify XML files (default is `none`)

  - `none` - do not minify XML files
  - `true` - minify XML files 

`minify_html` - minify HTML pages (default is `none`)

  - `none` - do not minify HTML files
  - `true` - minify HTML files 

`minify_js` - minify JavaScript files

  - `none` - do not minify JavaScript files
  - `true` - minify JavaScript files 

`minify_css` - minify CSS stylesheets

  - `none` - do not minify CSS files
  - `true` - minify CSS files 

#####Compression

`compress` - compress all generated files

  - `none` - do not compress generated files
  - `gzip` - compress all generated files using GZIP

###Deployment configuration

Deployment of final website is implemented as shell command execution.

`deploy` - a shell command or list of shell commands to deploy a website, that are executed sequentially
 
***Example***
``` groovy:nl
s3_bucket = "www.example.com"
deploy = "s3cmd sync --acl-public --reduced-redundancy ${destination_dir}/ s3://${s3_bucket}/"
```

###Environments
Different modes of Grain operation are associated with different configuration environments. These
environments can be used to provide environment specific configuration. 

***Example***
``` groovy:nl SiteConfig.groovy
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

`dev` - environment used when running Grain for website preview and development

`prod` - site generation and site deployment environment 

`cmd` - theme-specific command-mode environment, used when running a custom command
        defined in SiteConfig.groovy  
   
###Advanced configuration

####Source modifier closure

`source_modifier` - a closure that is run for each source file of website before rendering

***Parameters***:

  1. Resource file

***Return value***: transformed resource file text
  
***Example***

``` groovy:nl
source_modifier = { File file ->
    if (file.name.endsWith('.css')) {
        file.text.replaceAll('foo', 'bar')
    } else {
        file.text
    }
}
```

####Config posthandler hook

`config_posthandler` - a closure that is executed right after each execution of `SiteConfig.groovy`
 
***Example***

``` groovy:nl
config_posthandler = { println 'Config has been just rereaded' }
``` 

##Page structure

###Page file source

Typical Grain page consists of page header and page content. Page header contains static page configuration parameters
formatted using YAML markup. 
``` yaml:nl
---
layout: page
title: "Page title"
...
---
Page content goes here
```

Page content can be written in Markdown, HTML, XML or plain text. Grain determines content markup type based
on page file extension.

###Embedded Groovy code

Embedded Groovy code can be included anywhere in a page content, but not in the page header.
To include a simple Groovy expression one can use this notation:
``` groovy:nl
${2 + 2}
```

Or this
``` groovy:nl
<%= 2 + 2 %>
```
In both cases 4 will be inserted into the content of the page as the result of evaluation of these Groovy expressions.

To include large blocks of Groovy code one can use notation below:
``` grain:nl
<%
    def foo = 2 + 2
%>
...
<% if (foo == 4) { %>
   <span class="true"></span>
<% } %>
``` 

The `if` above will work as expected, e.g. the span will be rendered into page contents only when the criteria is met.
Also note that variables declared in one piece of embedded code will be available anywhere on the page.

###Variables on a page
Grain has several variables reserved, others can be freely introduced on a page.

These variables are reserved:

  - `page` - current page model
  - `site` - a facade to accessing website resources and configuration
  - `content` - HTML content of the page, accessible in layout
  - `out` - internal string buffer used to accumulate page contents during rendering

####Page variable

`page` variable provides access to all the keys of pages YAML headers.

For example to get page title one can use the following code snippet:
``` grain:nl
${page.title}
```

Grain generates the following header keys on initial loading of resource from source file:

  - `location` - relative page location on the filesystem, for example `/index.html`
                 or `/contacts/index.markdown`
  - `url` - URL of the page
  - `type` - resource type, either 'page' for resources that render into HTML or `asset` for all the other
             resources
  - `dateCreated` - resource file creation time in milliseconds
  - `lastUpdated` - resource file last update time in milliseconds
  - `text` - text contents of resource file
  - `bytes` - byte contents of resource file

`page` variable is just a Groovy map. You can add keys to this map in the page code
or make other changes, but these changes are only visible to the page itself,
they will not be visible to other pages.

####Site variable

`site` variable provides access to all the properties declared in SiteConfig.groovy. For example, in order to get
configured URL of the site one can use this code:
``` grain:nl
<a href="${site.url}">Home</a>
```

`site` also exposes all the pages of the site in `site.pages`, all the asset files of the site
in `site.assets` and both assets and pages in `site.resources`. For example to dump the value of title
defined in every site page one could do:
``` grain:nl
<%= site.pages.collect { it.title } %>
```

##Layouts

###Concept
Rendered page content usually wrapped up by layout, where most of the presentation logic is held.

Here is an example of a layout:
``` grain /theme/layout/default.html
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

###Layout nesting 
One layout can be based on another layout. For example, here is some page layout, based on default layout above:
``` grain /theme/layout/page.html
---
layout: default
title: "Default page title"
---
<div class="page">
    ${content}
</div>
```

Layout nesting can be unlimited.

##Includes

###Concept
Common page presentation parts can be kept in separate files and then included into layouts.

For example in the code below sidebar.html is included into layout:
``` grain /theme/layout/page.html
---
layout: default
title: "Default page title"
---
...
${include 'sidebar.html'}
```

###Passing custom model
In some cases you would want to pass some variables to the included parts:
``` grain /theme/layout/blog.html
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

##URL and resource mapping

###Introduction

It is obvious that in order to implement some complex tasks for your website, you may need to have full control over
your website resources. For example, you may want to customize website urls, add custom pages, like pagination pages or
pages that gathers posts related to certain tags, etc.
All sorts of this transformations can be made by using resource mapping mechanism. In order to start working with it,
it is important to be familiar with how website resources are represented in Grain.

###Resource representation
In Grain each resource is represented as plain Groovy map. Each resource should have at least two keys in its map:

- `location` - pointing to the resource file in filesystem
- `url` - representing the URL of the resource

Along with these keys, resource representation holds all the properties specified in content files' headers.

###Mapping customization

The list of all the resources after any site change is passed to the `SiteConfig.groovy -> resource_mapper` closure.
The closure is expected to build new list of resources that may have customized URLs, different resource variables, etc.

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

####Resource mapper closure

`resource_mapper` - a closure that is executed each time website changes for transforming initial resource models

***Parameters***:

  1. List of initial resource models

***Return value***: transformed resource models to be used for actual rendering of resources

***Example***
``` groovy:nl
resource_mapper = { resources ->
    resources.collect { Map resource ->
        if (resource.location =~/\/blog\/.*/) { //Select all the resources, which content files placed under /blog dir
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

##Tag libraries

###Standard tag library
Grain provides standard tags for most vital tasks, additional tags are recommended to be added to custom theme tag lib.
    
The standard tags are:

1. **`r`** - looks up resource URL by resource location
    
    ***Parameters***
      1. Resource location
          
    ***Example***
    ``` grain:nl
<link href="${r '/favicon.png'}" rel="icon"> ```
1. **`rs`** - looks up multiple resource URLs by their locations

    ***Parameters***
      1. Resource location list
      
    ***Example***
    ``` grain:nl
<% rs(['/javascripts/libs/jquery.min.js',
       '/javascripts/modernizr-2.0.js',
       '/javascripts/octopress.js',
       '/javascripts/github.js',
       '/javascripts/jquery.tweet.js',
       '/javascripts/twitter-options.js']).each { script -> %>
    <script src="${script}" type="text/javascript"></script>
<% } %> ```
1. **`include`** - inserts rendered resource contents

    ***Parameters***
      1. Template location
      1. *(Optional)* Additional model variables added to `page` map      

    ***Example***
    ``` grain:nl
${include 'tags.html', [tags: post.categories]} ```

###Custom tag libraries
You can add your own tags in your website theme. This can be made by implementing your tags as Groovy closures.

Class with tag closures should be added into the list of tag libs in `SiteConfig.groovy -> tag_libs`.
The tag lib class constructor should expect one argument - standard Grain taglib.
 
Example:
``` groovy:nl SiteConfig.groovy
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

##Custom command-line commands

###Creating your own commands
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

