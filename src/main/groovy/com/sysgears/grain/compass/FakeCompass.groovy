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

package com.sysgears.grain.compass
/**
 * Fake implementation of Compass integration - does nothing. 
 */
@javax.inject.Singleton
class FakeCompass extends AbstractCompass {

    /**
     * Does nothing
     * 
     * @param mode
     */
    public void configureAndLaunch(String mode) {
    }
    
    /**
     * Does nothing
     *
     * @param mode compass mode
     */
    public void launchCompass(String mode) {
    }

    /**
     * Awaits termination of Compass process
     */
    public void awaitTermination() {
    }

    /**
     * Shuts down Compass process 
     *
     * @throws Exception in case some error occur
     */
    @Override
    public void stop() {
    }

}
