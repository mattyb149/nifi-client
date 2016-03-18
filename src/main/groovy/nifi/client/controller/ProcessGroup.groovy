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
import nifi.client.Processors

import static groovyx.net.http.ContentType.URLENC
import static groovyx.net.http.Method.PUT

/**
 * Created by mburgess on 12/30/15.
 */
class ProcessGroup implements Map<String, Object> {
    NiFi nifi
    protected Map<String, Object> propertyMap = [:]
    protected Processors processors

    protected ProcessGroup(NiFi nifi, Map<String, Object> propMap, String parentId) {
        super()
        this.nifi = nifi
        this.propertyMap.putAll(propMap)
        this.propertyMap.put('parentId',parentId)
        this.propertyMap.put('groups', new HashMap<String,ProcessGroup>())
    }

    @Override
    int size() {
        return propertyMap.size()
    }

    @Override
    boolean isEmpty() {
        return propertyMap.isEmpty()
    }

    @Override
    boolean containsKey(Object key) {
        return propertyMap.containsKey(key)
    }

    @Override
    boolean containsValue(Object value) {
        return propertyMap.containsValue(value)
    }

    @Override
    Object get(Object key) {
        if('groups'.equals(key)) getGroups()
        if('processors'.equals(key)) propertyMap.put('processors', new Processors(nifi, id))
        return propertyMap.get(key)
    }

    @Override
    Object put(String key, Object value) {
        throw new UnsupportedOperationException('ProcessGroup property Map is immutable (for now)')
    }

    @Override
    String remove(Object key) {
        throw new UnsupportedOperationException('ProcessGroup property Map is immutable (for now)')
    }

    @Override
    void putAll(Map<? extends String, ? extends Object> m) {
        throw new UnsupportedOperationException('ProcessGroup property Map is immutable (for now)')
    }

    @Override
    void clear() {
        throw new UnsupportedOperationException('ProcessGroup property Map is immutable (for now)')
    }

    @Override
    Set<String> keySet() {
        return propertyMap.keySet()
    }

    @Override
    Collection<Object> values() {
        return propertyMap.values()
    }

    @Override
    Set<Map.Entry<String, Object>> entrySet() {
        return propertyMap.entrySet()
    }

    def set(Map props) {
        // Set the properties on the ProcessGroup and update it

    }

    def getGroups() {
        NiFi n = this.nifi
        def pm = this.propertyMap
        new JsonSlurper().parseText("${n.urlString}/nifi-api/controller/process-groups/${id}/process-group-references"
                .toURL().text)?.processGroups?.each { g ->
            pm.get('groups').put(g.name, new ProcessGroup(n, g as Map, this.id))
        }
        pm.get('groups')
    }

    def start() {
        updateState('RUNNING')
    }

    def stop() {
        updateState('STOPPED')
    }

    def enable() {
        updateState('STOPPED')
    }

    def disable() {
        updateState('DISABLED')
    }

    def updateState(String newState) {
        def parentGroupId = propertyMap.parentGroupId ?: 'root'
        def id = propertyMap.id
        def currentVersion = nifi.currentVersion

        try {

            nifi.http.request(PUT) { request ->
                uri.path = "/nifi-api/controller/process-groups/${parentGroupId}/process-group-references/${id}"
                def putBody = [version: currentVersion, state: newState]

                send URLENC, putBody

                response.'404' = { throw new Exception("Couldn't process start()") }

                //response.'409' = { resp, xml -> System.out << xml }

                response.success = { resp, xml ->
                    //println "Got code ${resp.statusLine.statusCode}"
                    switch (resp.statusLine.statusCode) {
                        case 200:
                            // TODO
                            break
                        case 201:
                            // Template was created successfully
                            //reload()
                            break

                        default:
                            // Something went wrong, throw an exception
                            //throw new Exception("Error importing $file")
                            break
                    }

                }
            }
        }
        catch (e) {
            e.printStackTrace(System.err)
        }
    }

}
