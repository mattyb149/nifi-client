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
class SystemDiagnostics implements Map<String, Object> {
    NiFi nifi
    private final JsonSlurper slurper = new JsonSlurper()
    protected final Map<String, Object> status = [:]
    private clientId

    protected SystemDiagnostics(NiFi nifi) {
        super()
        this.nifi = nifi
    }

    @Override
    int size() {
        reload()
        return status.size()
    }

    @Override
    boolean isEmpty() {
        reload()
        return status.isEmpty()
    }

    @Override
    boolean containsKey(Object key) {
        reload()
        return status.containsKey(key)
    }

    @Override
    boolean containsValue(Object value) {
        reload()
        return status.containsValue(value)
    }

    @Override
    Object get(Object key) {
        reload()
        return status.get(key)
    }

    @Override
    Object put(String key, Object value) {
        throw new UnsupportedOperationException('Processor Map is immutable (for now)')
    }

    @Override
    Object remove(Object key) {
        throw new UnsupportedOperationException('Processor Map is immutable (for now)')
    }

    @Override
    void putAll(Map<? extends String, ? extends Object> m) {
        throw new UnsupportedOperationException('Processor Map is immutable (for now)')
    }

    @Override
    void clear() {
        throw new UnsupportedOperationException('Processor Map is immutable (for now)')
    }

    @Override
    Set<String> keySet() {
        reload()
        status.keySet()
    }

    @Override
    Collection<Object> values() {
        reload()
        status.values()
    }

    @Override
    Set<Map.Entry<String, Object>> entrySet() {
        reload()
        status.entrySet()
    }


    def reload() {
        synchronized (this.status) {
            status.putAll( slurper.parseText("${nifi.urlString}/nifi-api/system-diagnostics".toURL().text)?.systemDiagnostics )
        }
    }
}
