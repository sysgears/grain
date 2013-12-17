##Overview

###Description
Grain is a static website building framework for Groovy that makes demanding static website implementation
an intuitive and enjoyable task to do. The framework provides a complete development mode with reload of changed
resources on the fly and website generation mode.

###Motivation
After developing several marketing and blogging websites using static website generators we have noticed that 
much of our time were spent.  

###Requirements

To run Grain framework you need [JDK 6 or later](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
set up. Download and install the appropriate JDK for your operating system.

###Installation

 1. Download the latest Grain distribution archive from the [GitHub page](https://github.com/sysgears/grain) and
 extract the zip file to a location of your choice. Alternatively you can clone grain git repository to the location
 of your choice.
 1. Build Grain framework binary by executing build script for your operating system,
    `./gradlew` for Unix/Linux/Mac OS X systems or `gradlew.bat` for Windows.
 1. Set the **GRAIN_HOME** environment variable to the location where you extracted the zip
    * On Unix/Linux systems add `export GRAIN_HOME=/path/to/grain` to your ~/.profile or /etc/profile
    * On Windows set the **GRAIN_HOME** variable under *My Computer/Advanced/Environment Variables*
 1. Add the Grain bin directory to your **PATH** variable
    * On Unix/Linux systems add `export PATH="$PATH:$GRAIN_HOME/bin"` to your profile
    * On Windows add the bin directory to the **PATH** environment variable under
      *My Computer/Advanced/Environment Variables*

##Getting Started

###Create website

To create a Grain based website you need to have Grain theme set up. Grain ships with the Octopress theme
in *grain/themes* directory. In order to use the provided theme, copy *grain/themes/octopress* directory
to the location of your choice.
``` sh:nl
cp -rf octopress /path/to/your_site
```

###Preview website
Navigate to the location of your newly created website `cd /path/to/your_site` and run the command
``` sh:nl
grain
```

to launch your website in preview mode. After that you can view your website by pointing web browser
to http://localhost:4000, then you can add/change/delete files of your website and see all the changes in your web
browser immediately after refreshing the page.

###Create new pages and posts

####Add new post
Now, when your website is up and running you could add new pages and posts and see them appearing.

To create new post execute:
``` sh:nl
grain new_post "My new post title"
```

This command will add the file `/content/blog/yyyy-mm-dd-my-new-post-title.markdown`. You can open this file
in any text editor and fill in author name and write some markdown text below the end of header:
``` yaml:nl
---
layout: post
title: "My new post title"
date: "2013-10-22 17:11"
author:
comments: true
categories: [article]
---
Your post text goes here
```

After you save the file you can see the result in web browser by navigating to your new post.

####Add new page
New pages get added the similar way:
``` sh:nl
grain new_page "/about/us" "About"
```

This will add the file `/content/about/us/index.markdown` representing the page. New page will show up in the 
site menu automatically, just refresh your browser.

To add new page in html format, execute the following command:
``` sh:nl
grain new_page "/contacts/index.html" "Contacts"
``` 

###Generate and deploy
When your site is ready for going live you can generate all the website files by executing
``` sh:nl
grain generate
```

The files of website will be generated to `/path/to/your_site/target` directory. 

You can deploy the resulting files either manually or with the help of Grain:
``` sh:nl
grain deploy
```
Check the deployment section for more information.

##IDE Integration

###IntelliJ IDEA

  1. Import project by selecting from menu: `File -> Import Project -> Select your website dir -> Gradle`
  1. Right click on theme/src/com.example.grain/Launcher and select `Run` to launch Grain in preview mode.

###Eclipse

####Prerequisites:

  1. Install [Groovy-Eclipse plugin](http://groovy.codehaus.org/Eclipse+Plugin) along with Groovy Compiler 2.1 Feature 

####Importing project:

  1. Generate Eclipse project files by executing `./gradlew eclipse` or `gradlew.bat eclipse` in website directory.
  1. Import project by selecting from menu: `File -> Import... -> Existing Projects Into Workspace -> Select your website dir`
  1. Right click on theme/src/com.example.grain/Launcher and select `Run As -> Groovy Script` to launch Grain in preview mode.

###NetBeans IDE

####Prerequisites:

  1. Install Groovy And Grails plugin, in the menu `Tools -> Plugins -> Available Plugins -> Groovy And Grails -> Install`

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
    - includes - common page parts, included from layouts 
    - layouts - page layouts
    - src - Groovy classes used from website pages
    - ... - other assets, organized the same way as in resulting website
  - SiteConfig.groovy - website configuration parameters
  
##Configuration

###Predefined variables

Grain passes minimal set of pre-defined variables to `SiteConfig.groovy`. These variables are:  

`site` - a reference to facade for accessing website resources and configuration

`log` - default logger

###Filesystem layout

Though in many situations you wouldn't touch default conventions, in some cases it is still beneficial
to have the  possibility for more fine-grained control over Grain website structure on the filesystem. 

The parameters below control website filesystem layout:

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

###Taglibs configuration

`tag_libs` - a list of tag library classes

***Example***
``` groovy:nl
tag_libs = [OctopressTagLib, PaginationTagLib]
```

###Resource mapping

`resource_mapper` - a closure that will be executed each time website changes for transforming initial resource models

***Parameters***:

  1. List of initial resource models

***Return value***: transformed resource models to be used for actual rendering of resources

***Example***
``` groovy:nl
resource_mapper = { resources ->
    resources.collect { Map resource ->
        if (resource.location =~/\/blog\/.*/) {
            // Rewrite url for blog posts
            def date = Date.parse(site.datetime_format, resource.date).format('yyyy/MM/dd/')
            def title = resource.title.encodeAsSlug()
            resource + [url: "/blog/$date$title/"]
        } else {
            resource
        }
    }
}
```

###Features configuration

Grain have many features provided by different implementations. Concrete implementations are specified in the `features` 
configuration section.

####Syntax highlighting feature

`highlight` - code highlighting method (default is `none`):

  - `none` - no code highlighting
  - `pygments` - code highlighting using Pygments   

`pygments` - Pygments integration method (default is `auto`):

  - `none` - do not use Pygments 
  - `python` - use Pygments bundled with Grain as Python process 
  - `jython` - use Pygments bundled with Grain as Jython process (slow startup time)
  - `shell` - use Pygments installed in the system as shell Python process
  - `auto` - use Pygments bundled with Grain as Python process if python is installed in the system,
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
  - `ruby` - use Compass bundled with Grain as Ruby process   
  - `jruby` - use Compass bundled with Grain as JRuby process (slow startup time, generally slower than Ruby process)   
  - `shell` - use Compass installed in the system as shell Ruby process
  - `auto` - use Compass bundled with Grain as Ruby process if ruby is installed in the system,
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
  
###Theme-specific command-line extension

It can be beneficial to add support for new custom commands to Grain command-line for pre-populating new pages or posts
or whatever.

All the commands supported by theme should be stored into `commands` list. Each command in this list is a closure
that accepts zero or more String parameters that will be passed from command-line.

***Example***
``` groovy:nl
commands = [
new_post: { String postTitle ->
    def date = new Date()
    def fileDate = date.format("yyyy-MM-dd")
    def filename = fileDate + "-" + postTitle.encodeAsSlug() + ".markdown"
    def file = new File(content_dir + "/blog/" + filename)
    file.exists() || file.write("""---
layout: post
title: "${postTitle}"
date: "${date.format(datetime_format)}"
author:
categories: []
comments: true
published: false
---
""")},
...
]
```

###Deployment configuration

Deployment of final website is implemented as shell command execution.

`deploy` - a shell command or list of shell commands to deploy a website, that will be executed sequentially
 
***Example***
``` groovy:nl
s3_bucket = "www.example.com"
deploy = "s3cmd sync --acl-public --reduced-redundancy ${destination_dir}/ s3://${s3_bucket}/"
```

###Environments
Different modes of Grain operation are associated with different configuration environments. These configuration
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

`cmd` - theme-specific command-mode environment, used when running an extension command
        defined in SiteConfig.groovy  
   
###Advanced configuration

####Source modifier closure

`source_modifier` - a closure that will be run for each source file of website before rendering

***Parameters***:

  1. Resource file

***Return value***: transformed resource file text
  
***Example***

``` groovy:nl
source_modifier = { File file ->
    if (file.name.endsWith('.css')) {
        file.text.replaceAll(/url\(\.[.\/]*([^)]+)\)/, {
            'url(${r "/' + it[1] + '"})'
        })
    } else {
        file.text
    }
}
```

####Config posthandler hook

`config_posthandler` - a closure that will be executed right after each execution of `SiteConfig.groovy`
 
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

Embedded Groovy code can be anywhere in page content, but not in the page header.
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
    def project = site.pages.find {
        it.url == page.url.find(~/(.*)docs/) { match, url -> url }
    }
%>
<span itemprop="title">${project.title}</span>
...
<% if (project.completed) { %>
   <span class="completed"></span>
<% } %>
``` 

The `if` above will work as expected, e.g. the span will be rendered into page contents only when the criteria is met.

###Variables on a page
Grain has several variables reserved, others can be freely introduced on a page.

These variables are reserved:

  - `site` - a facade to accessing website resources and configuration
  - `page` - current page model
  - `content` - HTML content of the page, accessible in layout
  - `out` - internal string buffer used to accumulate page contents during rendering
 
All the header keys are accessible on a page through `page` variable.

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
or make other changes, but these changes will be only visible to the page itself,
they will not be visible to other pages.
 
All the keys from SiteConfig.groovy are accessible through `site` variable. For example to get
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

Here is some example layout: 
``` grain /theme/layout/default.html
${include 'head.html'}

<body ${page.sidebar != false ? '' : "class='no-sidebar'"}>
  <header role="banner">${include 'header.html'}</header>
  <nav role="navigation">${include 'navigation.html'}</nav>
  <div id="main">
    <div id="content">
      ${content}
    </div>
  </div>
  <footer>${include 'footer.html'}</footer>
  ${include 'after_footer.html'}
</body>

</html>
```

Please note that rendered page contents will be passed in `content` variable to the layout.

###Layout nesting 
One layout can be based on another layout. For example, here is some page layout, based on default layout above:
``` grain /theme/layout/page.html
---
layout: default
title: "Default page title"
---
<div class="page">
  <article class="hentry" role="article">
    ${content}
  </article>
<% if (site.disqus.short_name && page.comments == true) { %>
  <section>
    <h1>Comments</h1>
    <div id="disqus_thread" aria-live="polite">${include 'post/disqus_thread.html'}</div>
  </section>
<% } %>
</div>
${include 'sidebar.html'}
```

Layout nesting can be unlimited. 

##Includes

###Concept
Common page presentation parts can be kept in separate files and then included into layouts.

For example in the example below sidebar.html included into layout:
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
             <a href="${post.url}" class="button medium color">Read More</a>
             ${include 'tags.html', [tags: post.categories]}
 
             <hr class="top bottom-2" />
           </footer>
```

After that, inside tags.html, you will have `page.tags` set to the value of `post.categories`.

##URL mapping

###In-memory resource representation
In Grain each resource is represented as plain Groovy map. Each resource should have at least two keys in its map:

- `location` - pointing to the file of the resources in filesystem
- `url` - representing the URL of the resource

###Mapping customization
The list of all the resources after any site change is passed to the `SiteConfig.groovy -> resource_mapper` closure.
The closure is expected to build new list of resources with customized URLs and other resource variables.
  
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
You can add own tags in your website theme. This is done by implementing your tags as Groovy closures.

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

