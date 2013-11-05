package com.example.grain.taglib

import com.sysgears.grain.taglib.GrainTagLib

class OctopressTagLib {

    /**
     * Grain taglib reference.
     */
    private GrainTagLib taglib

    public OctopressTagLib(GrainTagLib taglib) {
        this.taglib = taglib
    }

    /**
     * Renders a quote block which contains the quote text, author and source.
     *
     * @attr content the quote content
     * @attr author (optional) quote author
     * @attr sourceTitle (optional) title of the quote source
     * @athr sourceLink (optional) link to the quote source
     */
    def blockquote = { Map model = null ->
        // validates the tag attributes
        if (!model.content) throw new IllegalArgumentException('Tag [blockquote] is missing required attribute [content]')

        taglib.include('/tags/blockquote.html', [quote: model])
    }

    /**
     * Renders a content and duplicates the quote withing the content with a different formatting style.
     * <br />
     * The quote must be surrounded by '{/' '/}' tags, for instance: 'Lorem ipsum {/dolor/} sit amet'.
     *
     * @attr content content that contains a quote
     * @attr align (optional) quote position, can be either 'right' or 'left', 'right' is by default
     */
    def pullquote = { model ->
        // validates the tag attributes
        if (!model.content) throw new IllegalArgumentException('Tag [pullquote] is missing required attribute [content]')

        String content = model.content
        String align = model.align ?: 'right'

        // finds quote which is surrounded by '{/' '/}' tags
        String quote = content.find(/\{\/(.*)\/\}/) { match, quote -> quote }
        // removes '{/' '/}' tags from the content
        content = content.replace('{/', '').replace('/}', '')

        taglib.include('/tags/pullquote.html', [textblock: [content: content, quote: quote, align: align]])
    }

    /**
     * Embeds a gist into the page.
     *
     * @attr id unique gist identifier
     */
    def gist = { Map model = null ->
        // validates the tag attributes
        if (!model.id) throw new IllegalArgumentException('Tag [gist] is missing required attribute [id]')

        taglib.include('/tags/gist.html', [gist: model])
    }

    /**
     * Generates html tag for an image
     * 
     * @attr location image location
     * @attr width (optional) image width
     * @attr height (optional) image height
     */
    def img = { String location, Integer width = null, Integer height = null ->
        def widthStr = width ? " width=\"${width}\"" : ""
        def heightStr = height ? " height=\"${height}\"" : ""

        "<img${widthStr}${heightStr} src=\"${r(location)}\" alt=\"image\">"
    }

    /**
     * Embeds a video into the page.
     *
     * @attr url link to the video
     * @attr poster (optional) link to a poster
     * @attr wight (optional) video wight
     * @attr height (optional) video height
     */
    def video = { Map model = null ->
        // validates the tag attributes
        if (!model.url) throw new IllegalArgumentException('Tag [video] is missing required attribute [url]')

        def types = ['mp4': "video/mp4; codecs='avc1.42E01E, mp4a.40.2'",
                'ogv': "video/ogg; codecs=theora, vorbis",
                'webm': "video/webm; codecs=vp8, vorbis"] // supported video types

        // adds video type to the model
        model << [type: types."${model.url.find(/[^\.]+$/)}"]

        if (!model.type) throw new IllegalArgumentException("Tag [video] does not support [${model.url}] file format")

        taglib.include('/tags/video.html', [video: model])
    }
}
