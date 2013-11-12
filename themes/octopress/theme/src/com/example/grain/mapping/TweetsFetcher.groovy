package com.example.grain.mapping

import com.example.grain.twitter.TwitterApiConnector
import com.sysgears.grain.taglib.Site
import net.sf.json.JSONArray

import java.util.concurrent.TimeUnit

/**
 * Fetches the latest tweets through Twitter API. Uses local cache in development environment.
 */
class TweetsFetcher {

    /**
     * Provides access to the site configuration.
     */
    private Site site

    /**
     * Allows to connect to Twitter API.
     */
    private TwitterApiConnector twitter

    /**
     * File to cache tweets.
     */
    private File cacheFile

    public TweetsFetcher(Site site) {
        this.site = site
        twitter = new TwitterApiConnector(site)
    }

    /**
     * Returns the latest tweets.
     *
     * @param limit the maximum number of tweets to return
     *
     * @return the latest tweets if they are available, null otherwise
     */
    JSONArray getTweets(Integer limit) {
        prepareCacheFile()
        loadFromCache { tweets -> tweets.size() == limit } ?: loadFromTwitter(limit)
    }

    /**
     * Loads the latest tweets from Twitter.
     *
     * @param limit the maximum number of tweets to load
     *
     * @return the latest tweets or null in case of connection or API error
     */
    private JSONArray loadFromTwitter(Integer limit) {
        def tweets = twitter.getTweets(limit)
        // store tweets to the local cache
        cacheFile.write(tweets.inspect())
        tweets
    }

    /**
     * Loads tweets from the local cache if they matches the closure condition and the cache is still actual.
     *
     * @param closure a closure condition
     *
     * @return cached tweets or null if the cache is out of date or empty
     */
    private JSONArray loadFromCache(Closure closure) {
        JSONArray tweets = null

        if (cacheActual) {
            // loads tweets from the cache file
            tweets = Eval.me(cacheFile.text) as JSONArray
            // checks closure condition
            tweets = tweets && closure(tweets) ? tweets : null
        }

        tweets
    }

    /**
     * Creates cache file in case it doesn't exist.
     */
    private void prepareCacheFile() {
        if (!cacheFile) {
            def cacheDir = new File(site.cache_dir.toString(), 'grain')
            if (!cacheDir.exists()) cacheDir.mkdirs()
            cacheFile = new File(cacheDir, 'twitter.txt')
        }
    }

    /**
     * Checks whether the local cache is actual.
     *
     * @return true if the cache is actual, false otherwise
     */
    private boolean isCacheActual() {
        site.env == 'dev' && cacheFile.exists() &&
                (System.currentTimeMillis() - cacheFile.lastModified()) < TimeUnit.DAYS.toMillis(1)
    }
}
