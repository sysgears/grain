import com.example.grain.ResourceMapper
import com.example.grain.taglib.OctopressTagLib

/*
 * Grain configuration.
 */

// Resource mapper and tag libs.
resource_mapper = new ResourceMapper(site).map
tag_libs = [OctopressTagLib]

// Locale and datetime format.
Locale.setDefault(Locale.US)
dateTimeFormat = 'yyyy-MM-dd HH:mm'

// Site directories.
cache_dir = "${base_dir}/.cache"
content_dir = "${base_dir}/content"
theme_dir = "${base_dir}/theme"
source_dir = [content_dir, theme_dir, "${cache_dir}/compass"]
include_dir = "${theme_dir}/includes"
layout_dir = "${theme_dir}/layouts"
destination_dir = "${base_dir}/target"

excludes = ['/sass/.*', '/plugins/.*', '/target/.*']

features {
    highlight = "pygments"
    cache_highlight = "true"
    pygments = "auto"
    compass = "auto"
}

environments {
    dev {
        log.info "Development environment is used"
        jetty_port = 4000
        url = "http://localhost:${jetty_port}"
        show_unpublished = true
    }
    prod {
        log.info "Production environment is used"
        jetty_port = 4000
        url = "http://localhost:${jetty_port}"
        show_unpublished = false
        features {
            compress_xml = true
            compress_html = true
            compress_js = true
            compress_css = false
        }
    }
    cmd {
        features {
            highlight = "none"
            compass = "none"
        }
    }
}

// Deployment settings.
deploy = "s3"

s3_bucket = "www.example.com"
s3_deploy_cmd = "s3cmd sync --acl-public --reduced-redundancy ${destination_dir}/ s3://${s3_bucket}/"

rsync_ssh_user = "user@example.com"
rsync_ssh_port = "22"
rsync_document_root = "~/public_html/"
rsync_deploy_cmd = "rsync -avze 'ssh -p ${rsync_ssh_port}' --delete ${destination_dir} ${rsync_ssh_user}:${rsync_document_root}"

/*
 * Site configuration.
 */

// General settings.
title = 'Octopress theme for Grain' // blog name for the header, title and RSS feed
subtitle = 'Grain is a static web site building framework for Groovy' // blog brief description for the header
author = 'SysGears'                 // author name for Copyright, Metadata and RSS feed
meta_description = ''               // blog description for Metadata

// Blog and Archive.
posts_per_blog_page = 5             // the number of posts to display per blog page
posts_per_archive_page = 10         // the number of posts to display per archive page
disqus {
    short_name = ''                 // the unique identifier assigned to a Disqus (http://disqus.com/) forum
}

// RSS feed.
rss {
    feed = 'atom.xml'               // url to blog RSS feed
    email = ''                      // email address for the RSS feed
    post_count = 20                 // the number of posts in the RSS feed
}

// Site Search.
enable_site_search = true           // defines whether to enable site search

// Subscription by email.
subscribe_url = ''                  // url to subscribe by email (service integration required)

// Google Analytics.
google_analytics_tracking_id = ''   // google analytics tracking code, for details visit: http://www.google.com/analytics/

// Sharing.
sharing {
    // Button for sharing of posts and pages on Twitter.
    twitter {
        share_button {
            enabled = true
            lang = 'en'
        }
    }
    // Button for sharing of posts and pages on Facebook.
    facebook {
        share_button {
            enabled = true
            lang = 'en_US'          // locale code e.g. 'en_US', 'en_GB', etc.
        }
    }
    // Button for sharing of posts and pages on Google plus one.
    googleplus_one {
        share_button {
            enabled = true
            size = 'medium'         // one of 'small', 'medium', 'standard', 'tall'
        }
    }
}

// Sidebar modules that should be included by default.
default_asides = ['asides/recent_posts.html', 'asides/github.html', 'asides/tweets.html', 'asides/delicious.html',
        'asides/pinboard.html', 'asides/about.html', 'asides/facebook.html', 'asides/twitter.html',
        'asides/instagram.html', 'asides/google_plus.html']

asides {

    // Recent posts.
    recent_posts {
        count = 5
    }

    // Recent Delicious bookmarks.
    delicious {
        user = ''                   // Delicious (https://delicious.com/) username
        count = 5                   // the number of bookmarks to show
    }

    // Recent Pinboard bookmarks.
    pinboard {
        user = ''                   // Pinboard (http://pinboard.in/) username
        count = 5                   // the number of bookmarks to show
    }

    // GitHub repositories.
    github {
        user = 'sysgears'           // GitHub (https://github.com/) username
        show_profile_link = true    // whether to show link to GitHub profile
        skip_forks = true
        count = 10                  // the number of repositories to show
    }

    // The latest tweets.
    tweets {
        user = 'sysgears'           // Tweeter (https://twitter.com/) username
        count = 2                   // the number of tweets to display
        //consumer_key = ''         // to get consumer key and secret go to https://dev.twitter.com/apps and create a new application
        //consumer_secret = ''
        //access_token = ''
        //secret_token = ''
        follow_button {
            size = 'large'          // 'large' or 'medium'
            lang = 'en'             // one of 'en', 'fr', 'de', 'it', 'es', etc.
            show_name = true        // defines whether or not to show username
            show_count = true       // defines whether or not to show the number of followers
        }
    }

    // Links to social networks:
    google_plus {
        user = '109746189379932479469' // Google plus (https://plus.google.com/) user id
    }
    twitter {
        user = 'sysgears'           // Twitter (https://twitter.com/) username
    }
    facebook {
        user = 'sysgears'           // Facebook https://www.facebook.com/ username
    }
    instagram {
        user = ''                   // Instagram (http://instagram.com/) username
    }

    // Blog owner description.
    about_author = 'A brief description of blog owner.'
}

commands = [
new_post: { String postTitle ->
    def date = new Date()
    def fileDate = date.format("yyyy-MM-dd")
    def filename = fileDate + "-" + postTitle.encodeAsSlug() + ".markdown"
    def file = new File(content_dir + "/blog/" + filename)
    file.exists() || file.write("""---
layout: post
title: "${postTitle}"
date: "${date.format(dateTimeFormat)}"
author:
categories: []
comments: true
published: false
---
""")},
new_page: { String location, String pageTitle ->
        def ext = new File(location).extension
        def file
        if (!ext) {
            file = new File(content_dir + location, 'index.markdown')
        } else {
            file = new File(content_dir, location)
        }
        file.parentFile.mkdirs()
        file.exists() || file.write("""---
layout: page
title: "${pageTitle}"
navigate: true
---
""")}]
