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
package nifi.client.controller

import groovy.json.JsonSlurper
import nifi.client.NiFi

/**
 * Created by mburgess on 12/30/15.
 */
class Identity {
    NiFi nifi
    private final JsonSlurper slurper = new JsonSlurper()
    private clientId

    protected Identity(NiFi nifi) {
        super()
        this.nifi = nifi
    }

    def propertyMissing(String prop) {
        def s = slurper.parseText("${nifi.urlString}/nifi-api/controller/identity".toURL().text)
        clientId =  s?.revision?.clientId
        switch(prop) {
            case 'name': return s?.identity
            case 'userId': return s?.userId
            case 'clientId': return clientId
        }
        null
    }
}
