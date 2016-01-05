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
class Prioritizers implements Map<String, Object> {
    NiFi nifi
    private final JsonSlurper slurper = new JsonSlurper()
    protected final Map<String, Object> prioritizers = [:]

    protected Prioritizers(NiFi nifi) {
        super()
        this.nifi = nifi
    }


    @Override
    int size() {
        reload()
        return prioritizers.size()
    }

    @Override
    boolean isEmpty() {
        reload()
        return prioritizers.isEmpty()
    }

    @Override
    boolean containsKey(Object key) {
        reload()
        return prioritizers.containsKey(key)
    }

    @Override
    boolean containsValue(Object value) {
        reload()
        return prioritizers.containsValue(value)
    }

    @Override
    Object get(Object key) {
        reload()
        return prioritizers.get(key)
    }

    @Override
    Object put(String key, Object value) {
        throw new UnsupportedOperationException('Prioritizer Map is immutable (for now)')
    }

    @Override
    Object remove(Object key) {
        throw new UnsupportedOperationException('Prioritizer Map is immutable (for now)')
    }

    @Override
    void putAll(Map<? extends String, ? extends Object> m) {
        throw new UnsupportedOperationException('Prioritizer Map is immutable (for now)')
    }

    @Override
    void clear() {
        throw new UnsupportedOperationException('Prioritizer Map is immutable (for now)')
    }

    @Override
    Set<String> keySet() {
        reload()
        prioritizers.keySet()
    }

    @Override
    Collection<Object> values() {
        reload()
        prioritizers.values()
    }

    @Override
    Set<Map.Entry<String, Object>> entrySet() {
        reload()
        prioritizers.entrySet()
    }


    private String getSimpleName(String name) {
        name[(name.lastIndexOf('.')+1)..(-1)]
    }

    Collection<String> types() {
        values().collect { getSimpleName(it.type) }.unique()
    }

    def reload() {
        synchronized (this.prioritizers) {
            def procs = slurper.parseText("${nifi.urlString}/nifi-api/controller/prioritizers".toURL().text).prioritizerTypes
            def map = this.prioritizers
            procs.each { p ->
                map.put(getSimpleName(p.type), p)
            }
        }
    }
}
