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

import static groovyx.net.http.ContentType.URLENC
import static groovyx.net.http.Method.DELETE
import static groovyx.net.http.Method.POST

/**
 * An object corresponding to a provenance event
 */
class ProvenanceEvent implements Map<String, Object> {
    NiFi nifi
    protected Map<String, Object> propertyMap = [:]
    Map<String, Object> results = [:]

    public static final JsonSlurper slurper = new JsonSlurper()

    protected ProvenanceEvent(NiFi nifi, Map<String, Object> propMap) {
        super()
        this.nifi = nifi
        this.propertyMap = propMap
        //println "Adding a new prov event with properties: $propMap"
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
        if(key == 'results') return results;
        return propertyMap.get(key)
    }

    @Override
    Object put(String key, Object value) {
        throw new UnsupportedOperationException('Processor property Map is immutable (for now)')
    }

    @Override
    Object remove(Object key) {
        throw new UnsupportedOperationException('Processor property Map is immutable (for now)')
    }

    @Override
    void putAll(Map<? extends String, ? extends Object> m) {
        throw new UnsupportedOperationException('Processor property Map is immutable (for now)')
    }

    @Override
    void clear() {
        throw new UnsupportedOperationException('Processor property Map is immutable (for now)')
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

    def lineage() {

        // Need to get latest revision
        def latestVersion = nifi.currentVersion
        def options = [
                version           : latestVersion,
                lineageRequestType: 'FLOWFILE',
                uuid              : propertyMap.get('flowFileUuid')
        ]
        try {
            String retryUrl
            final JsonSlurper slurp = slurper
            nifi.http.request(POST) {
                uri.path = '/nifi-api/controller/provenance/lineage'
                send URLENC, options

                response.success = { instanceResp, json ->
                    retryUrl = parseResponse(json)

                    while (retryUrl) {
                        Thread.sleep(250)
                        retryUrl = parseResponse(slurp.parseText(retryUrl.toURL().text))
                    }
                }
                response.'404' = { throw new Exception("Couldn't find lineage query with ID $tID") }
            }
        } catch (e) {
            e.printStackTrace(System.err)
        }
        results
    }

    private String parseResponse(json) {
        def responseId = json.lineage.id
        // Check for completion
        if (json.lineage.finished) {
            results = json.lineage.results
            nifi.http.request(DELETE) { uri.path = "/nifi-api/controller/provenance/lineage/$responseId" }
            return null
        } else {
            return json.lineage.uri
        }
    }
}
