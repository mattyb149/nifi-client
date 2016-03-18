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
class ProcessGroups implements Map<String, ProcessGroup> {
    NiFi nifi
    private final JsonSlurper slurper = new JsonSlurper()
    private String processGroup
    protected final Map<String, ProcessGroup> processorGroupMap = [:]

    protected ProcessGroups(NiFi nifi, String pGroup) {
        super()
        this.nifi = nifi
        this.processGroup = pGroup ?: 'root'
    }

    @Override
    int size() {
        reload()
        return processorGroupMap.size()
    }

    @Override
    boolean isEmpty() {
        reload()
        return processorGroupMap.isEmpty()
    }

    @Override
    boolean containsKey(Object key) {
        reload()
        return processorGroupMap.containsKey(key)
    }

    @Override
    boolean containsValue(Object value) {
        reload()
        return processorGroupMap.containsValue(value)
    }

    @Override
    ProcessGroup get(Object key) {
        reload()
        return processorGroupMap.get(key)
    }

    @Override
    ProcessGroup put(String key, ProcessGroup value) {
        throw new UnsupportedOperationException('ProcessGroup Map is immutable (for now)')
    }

    @Override
    ProcessGroup remove(Object key) {
        throw new UnsupportedOperationException('ProcessGroup Map is immutable (for now)')
    }

    @Override
    void putAll(Map<? extends String, ? extends ProcessGroup> m) {
        throw new UnsupportedOperationException('ProcessGroup Map is immutable (for now)')
    }

    @Override
    void clear() {
        throw new UnsupportedOperationException('ProcessGroup Map is immutable (for now)')
    }

    @Override
    Set<String> keySet() {
        reload()
        processorGroupMap.keySet()
    }

    @Override
    Collection<Object> values() {
        reload()
        processorGroupMap.values()
    }

    @Override
    Set<Map.Entry<String, Object>> entrySet() {
        reload()
        processorGroupMap.entrySet()
    }

    def reload() {
        def pGroup = processGroup ?: 'root'
        println "Fetching ${nifi.urlString}/nifi-api/controller/process-groups/${pGroup}"
        synchronized (this.processorGroupMap) {
            def procs = slurper.parseText("${nifi.urlString}/nifi-api/controller/process-groups/${pGroup}".toURL().text).processGroups
            def map = this.processorGroupMap
            def n = this.nifi
            procs.each { p ->
                map.put(p.name, new ProcessGroup(n, p))
            }
        }
    }
}
