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
class History implements Map<String, Object> {
    NiFi nifi
    private final JsonSlurper slurper = new JsonSlurper()
    protected final Map<String, Object> history = [:]
    private offset = 0
    private count = 100
    private sortOrder
    private sortColumn
    private startDate
    private endDate
    private userName
    private sourceId

    protected History(NiFi nifi) {
        super()
        this.nifi = nifi
    }


    @Override
    int size() {
        reload()
        return history.size()
    }

    @Override
    boolean isEmpty() {
        reload()
        return history.isEmpty()
    }

    @Override
    boolean containsKey(Object key) {
        reload()
        return history.containsKey(key)
    }

    @Override
    boolean containsValue(Object value) {
        reload()
        return history.containsValue(value)
    }

    @Override
    Object get(Object key) {
        reload()
        return history.get(key)
    }

    @Override
    Object put(String key, Object value) {
        throw new UnsupportedOperationException('ControllerServiceTypes Map is immutable (for now)')
    }

    @Override
    Object remove(Object key) {
        throw new UnsupportedOperationException('ControllerServiceTypes Map is immutable (for now)')
    }

    @Override
    void putAll(Map<? extends String, ? extends Object> m) {
        throw new UnsupportedOperationException('ControllerServiceTypes Map is immutable (for now)')
    }

    @Override
    void clear() {
        throw new UnsupportedOperationException('ControllerServiceTypes Map is immutable (for now)')
    }

    @Override
    Set<String> keySet() {
        reload()
        history.keySet()
    }

    @Override
    Collection<Object> values() {
        reload()
        history.values()
    }

    @Override
    Set<Map.Entry<String, Object>> entrySet() {
        reload()
        history.entrySet()
    }


    def reload(paramMap) {
        synchronized (this.history) {
            def queryParams = [] as List

            def o = paramMap?.offset ?: offset
            if(o != null) queryParams.add("offset=${offset = o}")

            def ct = paramMap?.count ?: count
            if(ct != null) queryParams.add("count=${count = ct}")

            def so = paramMap?.sortOrder ?: sortOrder
            if(so) queryParams.add("sortOrder=${sortOrder = so}");

            def sd = paramMap?.startDate ?: startDate
            if(sd) queryParams.add("startDate=${startDate = sd}");

            def ed = paramMap?.endDate ?: endDate
            if(ed) queryParams.add("endDate=${endDate = ed}");

            def un = paramMap?.userName ?: userName
            if(un) queryParams.add("userName=${userName = un}");

            def si = paramMap?.sourceId ?: sourceId
            if(si) queryParams.add("sourceId=${sourceId = si}");

            def queryString = queryParams.join('&')
            this.history.clear()
            this.history.putAll(slurper.parseText("${nifi.urlString}/nifi-api/controller/history?${queryString}".toURL().text).history)
        }
    }
}
