package com.example.grain

import com.example.grain.mapping.Paginator
import com.example.grain.mapping.TweetsFetcher
import com.sysgears.grain.taglib.Site
import com.sysgears.grain.taglib.GrainUtils

/**
 * Change pages urls and extend models.
 */
class ResourceMapper {

    /**
     * Site reference, provides access to site configuration.
     */
    private final Site site

    /**
     * Allows to load the latest tweets.
     */
    private TweetsFetcher tweetsFetcher

    public ResourceMapper(Site site) {
        this.site = site
        tweetsFetcher = new TweetsFetcher(site)
    }

    /**
     * This closure is used to map pages.
     */
    def map = { resources ->
        // excludes resources with published property set to false,
        // unless it is allowed to show unpublished resources in SiteConfig.
        def publishedResources = resources.findAll { it.published != false || site.show_unpublished }

        customizeModels << addSiteMenu << customizeAsides << customizeUrls << publishedResources
    }

    /**
     * Customizes pages urls.
     */
    private def customizeUrls = { List resources ->
        def tweets = tweetsFetcher.getTweets(site.asides.tweets.count)?: []

        resources.collect { Map resource ->
            def location = resource.location
            def customUrl = resource.url
            switch (location) {
                case ~/\/(images|javascripts|stylesheets)\/(?!icons).*/:
                    if (location == '/javascripts/jquery.tweet.js') {
                        resource += [tweets: tweets.toString().replace('\\', '\\\\')]
                    }
                    customUrl = getFingerprintUrl(resource)
                    break
                case ~/\/blog\/.*/:
                    customUrl = getPostUrl('/blog/', resource)
                    break
                case ~/\/errors.*/:
                    customUrl = "/errors/"
                    break
            }

            resource + [url: customUrl]
        }
    }

    /**
     * Builds site menu and adds it to all pages
     */
    private def addSiteMenu = { List resources ->
        def siteMenu = resources.findResults { it.navigate ? [it.url, it.menu_title ?: it.title] : null }.sort { it[0] }

        resources.collect { Map resource ->
            if (resource.type == 'page') {
                resource + [siteMenu: siteMenu]
            } else {
                resource
            }
        }
    }


    /**
     * Customizes pages models, applies pagination (creates new pages)
     */
    private def customizeModels = { List resources ->
        def posts = resources.findAll { it.layout == 'post' }
        Set<String> tags = posts.inject([]) { List tags, Map post -> tags + post.categories }

        def postsByCategory = { tag -> posts.findAll { post -> tag in post.categories } }

        def postsByAuthor = posts.groupBy { it.author }

        resources.inject([]) { List updatedResources, Map page ->
            def applyPagination = { items, perPage, url, model = [:] ->
                updatedResources += Paginator.paginate(items, 'posts', perPage, url, page + model)
            }
            switch (page.url) {
                case '/':
                    applyPagination(posts, site.posts_per_page, page.url)
                    break
                case '/archives/':
                    applyPagination(posts, site.archives_per_page, page.url)
                    break
                case '/authors/':
                    postsByAuthor.each { String author, List items ->
                        if (author) {
                            applyPagination(items, site.posts_per_page, "${page.url}${author.encodeAsSlug()}/", [author: author])
                        }
                    }
                    break
                case '/categories/':
                    tags.each { String tag ->
                        applyPagination(postsByCategory(tag), site.posts_per_page, "${page.url}${tag.encodeAsSlug()}/", [tag: tag])
                    }
                    break
                case '/atom.xml':
                    int maxRss = site.rss_post_count
                    def lastUpdated = posts.size() > 0 ? Date.parse(site.dateTimeFormat, posts[0].updated).time : ''
                    // default feed
                    updatedResources << (page + [posts: posts.take(maxRss), lastUpdated: lastUpdated])

                    // feed for each category
                    updatedResources += tags.collect { String tag ->
                        def feedUrl = "/categories/${tag.encodeAsSlug()}/atom.xml"
                        page + [url: feedUrl, tag: tag, posts: postsByCategory(tag).take(maxRss)]
                    }
                    break
                case ~/\/blog\/.*/:
                    def post = posts.find { it.url == page.url }
                    def index = posts.indexOf(post)
                    def prev = index > 0 ? posts[index - 1] : null
                    def next = posts[index + 1]
                    updatedResources << (page + [prev_post: prev, next_post: next])
                    break
                default:
                    updatedResources << page
            }

            updatedResources
        }
    }

    /**
     * Customizes asides data.
     */
    private def customizeAsides = { List resources ->
        def posts = resources.findAll { it.layout == 'post' }
        def max = site.asides?.recent_posts?.count ?: 5
        def recentPosts = posts.take(max).collect { post -> [url: post.url, title: post.title] }

        resources.collect { Map page ->
            def asides = page.asides ?: site.default_asides
            if ('asides/recent_posts.html' in asides) {
                page + [recent_posts: recentPosts]
            } else {
                page
            }
        }
    }

    /**
     * Creates URL for a post page. The URL format is '{base path}/{year}/{month}/{day}/{post name}'.
     *
     * @param basePath base path to the page
     * @param location location of the file
     *
     * @return formatted url to the post page
     */
    private String getPostUrl(String basePath, Map resource) {
        def date = Date.parse(site.dateTimeFormat, resource.date).format('yyyy/MM/dd/')
        def title = resource.title.encodeAsSlug()
        "$basePath$date$title/"
    }

    /**
     * Creates fingerprint URL to the resource.
     *
     * @param resource model of the resource
     *
     * @return fingerprint URL to the resource.
     */
    private String getFingerprintUrl(Map resource) {
        def extension = new File(resource.location as String).getExtension()
        def resourceName = resource.location - ".${extension}"
        "${resourceName}-${GrainUtils.md5(resource.render().bytes)}.${extension}"
    }
}
