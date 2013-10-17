import com.example.grain.ResourceMapper
import com.example.grain.OctopressTagLib

Locale.setDefault(Locale.US)

dateTimeFormat = 'yyyy-MM-dd HH:mm'
excludes = ["/sass/.*", "/images/icons/.*", "/plugins/.*", "/target/.*"]

environments {
    dev {
        log.info "Development environment is used"
        jetty_port = 4000
        url = "http://localhost:${jetty_port}"
        show_unpublished = true
        javascripts = ['/javascripts/libs/jquery.min.js',
            '/javascripts/modernizr-2.0.js',
            '/javascripts/octopress.js',
            '/javascripts/twitter.js',
            '/javascripts/github.js']
    }
    prod {
        log.info "Production environment is used"
        jetty_port = 4000
        features {
            compress_xml = true
            compress_html = true
            compress_js = true
            compress_css = false
        }
        url = "http://localhost:${jetty_port}"
        javascripts = ['/javascripts/site.js']
        show_unpublished = false
    }
    cmd {
        features {
            highlight = "none"
            compass = "none"
        }
    }
}

sharing {
  twitter {
    share_button {
      enabled = true
      lang = 'en'
    }
  }
  facebook {
    share_button {
      enabled = true
      lang = 'en_US' // locale code e.g. 'en_US', 'ru_RU', etc.
    }
  }
  googleplus_one {
    share_button {
      enabled = true
      size = 'medium' // one of 'small', 'medium', 'standard', 'tall'
    }
  }
}

disqus {
  show_comment_count = true
  short_name = 'grain'
}

asides {
  recent_posts {
      count = 5
      hidden = false
  }
  googleplus_one {
      user = 'username'
      hidden = false
  }
  twitter {
      user = 'grailsplugins'
      hidden = false
      tweets {
        enabled = true
        count = 5
        replies = false
      }
      follow_button {
        enabled = true
        count = false // false or true refer to facebook documentation.
        size = 'large'
        show_count = false
        lang = 'en'
      }
  }
  delicious {
      user = 'steve'
      bookmarks {
        enabled = true
        count = 5
      }
      hidden = false
  }
  instagram {
      user = 'johny'
      hidden = false
  }
  facebook {
      user = 'johny'
      hidden = false
  }
  github {
      user = 'johny'
      show_profile_link = true
      skip_forks = true
      repo_count = 10
      hidden = false
  }
  pinboard {
      user = 'Serge'
      hidden = false
      bookmarks {
        enabled = true
        count = 5
      }
  }
}

features {
    highlight = "pygments"
    cache_highlight = "true"
    pygments = "auto"
    compass = "auto"
}


title = "My Grain Blog"
subtitle = "A static web site building framework"
author = "Your name"
about = "A little something about me."
email = "info@example.com"
simple_search = "http://google.com/search"
subscribe_rss = "atom.xml"
subscribe_email = "username@gmail.com"
// google_analytics_tracking_id = "UA-XXXXXXXX-X"
api_key = "kwJiszKArE6455wVlPt8zQrTrwIRUubnC63F8kiwj00lCcfzNkk5RIBDPpwmbDJQ"
forum_name = "example"
posts_per_page = 5
archives_per_page = 10
rss_post_count = 20
debug = false
default_asides = ['asides/recent_posts.html', 'asides/tweets.html', 'asides/github.html', 'asides/delicious.html',
        'asides/pinboard.html', 'custom/asides/about.html', 'asides/facebook.html', 'asides/twitter.html',
        'asides/instagram.html', 'asides/google_plus.html']

resource_mapper = new ResourceMapper(site).map
tag_libs = [OctopressTagLib]

cache_dir = "${base_dir}/.cache"
content_dir = "${base_dir}/content"
theme_dir = "${base_dir}/theme" 
source_dir = [content_dir, theme_dir, "${cache_dir}/compass"]
include_dir = "${theme_dir}/includes"
layout_dir = "${theme_dir}/layouts"
destination_dir = "${base_dir}/target"

commands = [
new_post: { String postTitle ->
    def date = new Date()
    def fileDate = date.format("yyyy-MM-dd")
    def filename = fileDate + "-" + postTitle.encodeAsSlug() + ".markdown"
    def file = new File(content_dir + "/_posts/" + filename)
    file.exists() || file.write("""---
layout: post
title: "${postTitle}"
date: "${dateTimeFormat.format(date)}"
author:
comments: true
categories: [article]
---
""")
        }]

deploy = "s3"
s3_bucket = "www.example.com"
s3_deploy_cmd = "s3cmd sync --acl-public --reduced-redundancy ${destination_dir}/ s3://${s3_bucket}/"

rsync_ssh_user = "user@example.com"
rsync_ssh_port = "22"
rsync_document_root = "~/public_html/"
rsync_deploy_cmd = "rsync -avze 'ssh -p ${rsync_ssh_port}' --delete ${destination_dir} ${rsync_ssh_user}:${rsync_document_root}"
