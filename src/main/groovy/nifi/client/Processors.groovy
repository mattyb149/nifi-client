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
class Processors implements Map<String, Processor> {
    NiFi nifi
    private final JsonSlurper slurper = new JsonSlurper()
    private String processGroup
    protected final Map<String, Processor> processorIdMap = [:]

    protected Processors(NiFi nifi) {
        super()
        this.nifi = nifi
    }


    @Override
    int size() {
        reload()
        return processorIdMap.size()
    }

    @Override
    boolean isEmpty() {
        reload()
        return processorIdMap.isEmpty()
    }

    @Override
    boolean containsKey(Object key) {
        reload()
        return processorIdMap.containsKey(key)
    }

    @Override
    boolean containsValue(Object value) {
        reload()
        return processorIdMap.containsValue(value)
    }

    @Override
    Processor get(Object key) {
        reload()
        return processorIdMap.get(key)
    }

    @Override
    Processor put(String key, Processor value) {
        throw new UnsupportedOperationException('Processor Map is immutable (for now)')
    }

    @Override
    Processor remove(Object key) {
        throw new UnsupportedOperationException('Processor Map is immutable (for now)')
    }

    @Override
    void putAll(Map<? extends String, ? extends Processor> m) {
        throw new UnsupportedOperationException('Processor Map is immutable (for now)')
    }

    @Override
    void clear() {
        throw new UnsupportedOperationException('Processor Map is immutable (for now)')
    }

    @Override
    Set<String> keySet() {
        reload()
        processorIdMap.keySet()
    }

    @Override
    Collection<Object> values() {
        reload()
        processorIdMap.values()
    }

    @Override
    Set<Map.Entry<String, Object>> entrySet() {
        reload()
        processorIdMap.entrySet()
    }

    Collection<Processor> findByType(String type) {
        values().findAll { getSimpleName(it.type) == type }
    }

    private String getSimpleName(String name) {
        name[(name.lastIndexOf('.')+1)..(-1)]
    }

    Collection<String> types() {
        values().collect { getSimpleName(it.type) }.unique()
    }

    def reload() {
        synchronized (this.processorIdMap) {
            def procs = slurper.parseText("${nifi.urlString}/nifi-api/controller/process-groups/${processGroup ?: 'root'}/processors".toURL().text).processors
            def map = this.processorIdMap
            def n = this.nifi
            procs.each { p ->
                map.put(p.name, new Processor(n, p))
            }
        }
    }
}
