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
import groovyx.net.http.ContentType

import static groovyx.net.http.Method.PUT

/**
 * Created by mburgess on 12/30/15.
 */
class Processor implements Map<String, Object> {
    NiFi nifi
    private final JsonSlurper slurper = new JsonSlurper()
    protected final Map<String, Object> propertyMap = [:]
    protected Map<String, String> configProperties; // This is exposed as "properties" on the Processor object

    protected Processor(NiFi nifi, Map<String, Object> propMap) {
        super()
        this.nifi = nifi
        this.propertyMap.putAll(propMap)
        this.configProperties = new LinkedHashMap<String, String>(propMap.config.properties as Map) {
            @Override
            String put(String key, String value) {
                super.put(key, value)
                updateState([config: [properties: this]])
                value
            }

            /*@Override
            void putAll(Map<? extends String, ? extends String> m) {
                if (m) {
                    m.each { k, v ->
                        super.put(k, v)
                    }
                    updateState([config: [properties: this]])
                }
            }*/
        }
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
        // Need to check for our own members before delegating
        if ("properties".equals(key)) {
            return this.configProperties
        }
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

    def set(Map props) {
        // Set the properties on the Processor and update it
        propertyMap.putAll(props)
        updateState(props)
    }

    def start() {
        updateState(state: 'RUNNING')
    }

    def stop() {
        updateState(state: 'STOPPED')
    }

    def enable() {
        updateState(state: 'STOPPED')
    }

    def disable() {
        updateState(state: 'DISABLED')
    }

    def updateState(Map propMap) {
        def parentGroupId = propertyMap.parentGroupId
        def id = propertyMap.id
        def currentVersion = nifi.currentVersion

        try {

            nifi.http.request(PUT) { request ->
                uri.path = "/nifi-api/controller/process-groups/${parentGroupId}/processors/${id}"
                def putBody = ([revision: [version: currentVersion], processor: ([id: id] << propMap)])
                println putBody

                send ContentType.JSON, putBody

                response.'404' = { throw new Exception("Couldn't process start()") }

                //response.'409' = { resp, xml -> System.out << xml }

                response.success = { resp, xml ->
                    //println "Got code ${resp.statusLine.statusCode}"
                    switch (resp.statusLine.statusCode) {
                        case 200:
                            // TODO overwrite properties
                            break
                        case 201:
                            break

                        default:
                            // Something went wrong, throw an exception
                            throw new Exception("Error updating state with $propMap")
                            break
                    }

                }
            }
            propMap
        }
        catch (e) {
            e.printStackTrace(System.err)
        }
    }

    def history() {
        def id = propertyMap.id
        slurper.parseText("${nifi.urlString}/nifi-api/controller/history/processors/${id}".toURL().text)?.componentHistory
    }
}
