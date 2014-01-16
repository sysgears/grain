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

package com.sysgears.grain.rpc.python

import com.google.inject.AbstractModule
import com.google.inject.Injector
import com.google.inject.Provides
import com.sysgears.grain.config.ImplBinder

/**
 * Package-specific IoC config 
 */
class PythonModule extends AbstractModule {

    @Provides @javax.inject.Singleton
    public Python providePython(Injector injector,
            AutoPython auto, CPython cPython, Jython jython) {
        new ImplBinder<Python>(Python.class, 'features.python',
                [default: auto, auto: auto, python: cPython, jython: jython], injector).proxy
    }

    @Override
    protected void configure() {
    }
}
