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
class Controller {
    NiFi nifi
    About about
    Status status
    Banners banners
    BulletinBoard bulletins
    Config config
    ControllerServiceTypes controllerServiceTypes
    Identity identity
    Prioritizers prioritizers
    ProcessorTypes processorTypes
    ReportingTaskTypes reportingTaskTypes
    History history
    //ProcessGroup root TODO
    Provenance provenance
    private final JsonSlurper slurper = new JsonSlurper()
    private clientId
    private controller

    public Controller(NiFi nifi) {
        super()
        this.nifi = nifi
        this.about = new About(nifi)
        this.status = new Status(nifi)
        this.banners = new Banners(nifi)
        this.bulletins = new BulletinBoard(nifi)
        this.config = new Config(nifi)
        this.controllerServiceTypes = new ControllerServiceTypes(nifi)
        this.identity = new Identity(nifi)
        this.prioritizers = new Prioritizers(nifi)
        this.processorTypes = new ProcessorTypes(nifi)
        this.reportingTaskTypes = new ReportingTaskTypes(nifi)
        this.history = new History(nifi)
        //this.root = new ProcessGroup(nifi, [name:'root'])
        this.provenance = new Provenance(nifi)
    }

    def search(String query) {
        slurper.parseText("${nifi.urlString}/nifi-api/controller/search-results?q=$query".toURL().text)?.searchResultsDTO
    }

    def history(Map queryParams) {
        this.history.reload(queryParams)
        this.history
    }

    def propertyMissing(String name) {

        def s = slurper.parseText("${nifi.urlString}/nifi-api/controller".toURL().text)
        controller = s?.controller
        clientId = s?.revision?.clientId
        controller[name]

    }
}
