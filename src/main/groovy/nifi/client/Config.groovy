/*******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package nifi.client

import groovy.json.JsonSlurper

/**
 * Created by mburgess on 12/30/15.
 */
class Config {
    NiFi nifi
    private final JsonSlurper slurper = new JsonSlurper()
    private Object config
    private clientId

    protected Config(NiFi nifi) {
        super()
        this.nifi = nifi
    }

    def propertyMissing(String name) {
        def s = slurper.parseText("${nifi.urlString}/nifi-api/controller/config".toURL().text)
        config = s?.config
        clientId =  s?.revision?.clientId
        config[name]
    }
}
