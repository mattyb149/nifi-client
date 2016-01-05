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
class Processor implements Map<String,String> {
    NiFi nifi
    private final JsonSlurper slurper = new JsonSlurper()
    protected Map<String,String> propertyMap = [:]

    protected Processor(NiFi nifi, Map<String,String> propMap) {
        super()
        this.nifi = nifi
        this.propertyMap = propMap
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
    String get(Object key) {
        return propertyMap.get(key)
    }

    @Override
    String put(String key, String value) {
        throw new UnsupportedOperationException('Processor property Map is immutable (for now)')
    }

    @Override
    String remove(Object key) {
        throw new UnsupportedOperationException('Processor property Map is immutable (for now)')
    }

    @Override
    void putAll(Map<? extends String, ? extends String> m) {
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

    }

    def start() {

    }

}
