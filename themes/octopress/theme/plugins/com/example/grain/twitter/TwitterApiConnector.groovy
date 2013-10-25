package com.example.grain.twitter

import com.sysgears.grain.taglib.Site
import groovyx.net.http.RESTClient
import net.sf.json.JSONArray

import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.Method.GET

/**
 * Allows to connect to Twitter API version 1.1.
 */
class TwitterApiConnector {

    /**
     * HTTP builder to connect to twitter API.
     */
    private RESTClient rest = new RESTClient('https://api.twitter.com')

    /**
     * Site reference, allows to access site configuration.
     */
    private Site site

    TwitterApiConnector(site) {
        this.site = site
    }

    /**
     * Sends request to the Twitter to obtain user tweets.
     *
     * @param limit the number of tweets to retrieve
     *
     * @return JSONObject with response or null if server respond with error status code
     */
    JSONArray getTweets(Integer limit) {
        sendRequest('/1.1/statuses/user_timeline.json', ['screen_name': site.asides.tweets.user, 'count': limit])
    }

    /**
     * Sends request to twitter API.
     *
     * @param resource resource to send request to
     * @param query parameters map that represents request query string
     *
     * @return JSONObject with response or null if server respond with error status code
     */
    private JSONArray sendRequest(String resource, Map query) {
        // gets OAuth credentials
        String consumerKey = site.asides.tweets.consumer_key ?: ''
        String consumerSecret = site.asides.tweets.consumer_secret ?: ''
        String accessToken = site.asides.tweets.access_token ?: ''
        String secretToken = site.asides.tweets.secret_token ?: ''

        def response
        if (consumerKey && consumerSecret && accessToken && secretToken) {
            // sends OAuth signed request
            rest.auth.oauth(consumerKey, consumerSecret, accessToken, secretToken)
            def resp = rest.request(GET, JSON) { req ->
                uri.path = resource
                uri.query = query
                req.getParams().setParameter('http.socket.timeout', 5000)
            }
            // checks response code and returns the JSON data
            if (resp.status == 200) {
                response = resp.data
            } else {
                println "ERROR occurred in TwitterApiConnector when fetching tweets: \n" + resp.data
                response = null
            }
        } else {
            println "WARN: tweets won't be fetched as Twitter API credentials not set in configurations"
            response = null
        }

        response
    }
}
