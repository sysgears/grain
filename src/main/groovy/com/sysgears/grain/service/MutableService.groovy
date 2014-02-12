package com.sysgears.grain.service

import com.sysgears.grain.preview.ConfigChangeListener

/**
 * Created by victor on 2/10/14.
 */
interface MutableService extends Service, ConfigChangeListener {
    public String getMessage()
}
