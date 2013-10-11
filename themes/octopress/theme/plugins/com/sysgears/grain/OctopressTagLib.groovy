package com.sysgears.grain

import com.sysgears.grain.taglib.GrainTagLib

class OctopressTagLib {
    private GrainTagLib taglib

    public OctopressTagLib(GrainTagLib taglib) {
        this.taglib = taglib
    }

    def video = { Map model = null ->
        taglib.include('/tags/video.html', model)
    }

    def gist = { Map model = null ->
        taglib.include('/tags/gist.html', model)
    }

    def blockquote = { Map model = null ->
        taglib.include('/tags/blockquote.html', model)
    }

    def pullquote = { Map model = null ->
        taglib.include('/tags/pullquote.html', model)
    }
}
