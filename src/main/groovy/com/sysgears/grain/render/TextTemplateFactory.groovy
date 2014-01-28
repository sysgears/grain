/*
 * Copyright (c) 2013 SysGears, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sysgears.grain.render

import com.google.inject.assistedinject.Assisted

import javax.annotation.Nullable

/**
 * TextTemplate factory.
 */
public interface TextTemplateFactory {
    
    /**
     * Creates an instance of TextTemplate
     *
     * @param file source file
     * @param contents resource file text contents
     */
    public TextTemplate create(@Assisted final File file,
                               @Assisted final String contents)
}