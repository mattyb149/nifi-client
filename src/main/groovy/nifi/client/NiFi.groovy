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
import groovyx.net.http.HTTPBuilder
import nifi.client.controller.Controller

/**
 * Created by mburgess on 12/30/15.
 */
class NiFi {
    String urlString
    Processors processors
    Templates templates
    Controller controller
    SystemDiagnostics system
    def http

    private JsonSlurper slurper = new JsonSlurper()

    private NiFi(String url) {
        this.urlString = url
        this.http = this.http = new HTTPBuilder(url)
        this.processors = new Processors(this)
        this.templates = new Templates(this)
        this.controller = new Controller(this)
        this.system = new SystemDiagnostics(this)
    }

    static NiFi bind(URL url) {
        return new NiFi(url.toString())
    }

    static NiFi bind(String url) {
        return new NiFi(url)
    }

    long getCurrentVersion() {
        return slurper.parseText("${urlString}/nifi-api/controller/revision".toURL().text)?.revision?.version ?: -1
    }

}
