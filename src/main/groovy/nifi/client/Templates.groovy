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

import static groovyx.net.http.Method.POST
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.StringBody

/**
 * Created by mburgess on 12/30/15.
 */
class Templates implements Map<String, Template> {

    NiFi nifi
    protected final JsonSlurper slurper = new JsonSlurper()
    protected final Map<String, Template> templateIdMap = [:]

    protected Templates(NiFi nifi) {
        super()
        this.nifi = nifi

    }


    @Override
    int size() {
        reload()
        return templateIdMap.size()
    }

    @Override
    boolean isEmpty() {
        reload()
        return templateIdMap.isEmpty()
    }

    @Override
    boolean containsKey(Object key) {
        reload()
        return templateIdMap.containsKey(key)
    }

    @Override
    boolean containsValue(Object value) {
        reload()
        return templateIdMap.containsValue(value)
    }

    @Override
    Template get(Object key) {
        reload()
        return templateIdMap.get(key)
    }

    @Override
    Template put(String key, Template value) {
        throw new UnsupportedOperationException('Template Map is immutable (for now)')
    }

    @Override
    Template remove(Object key) {
        Template t = templateIdMap.get(key)
        t.delete()
        return t
    }

    @Override
    void putAll(Map<? extends String, ? extends Template> m) {
        throw new UnsupportedOperationException('Template Map is immutable (for now)')
    }

    @Override
    void clear() {
        throw new UnsupportedOperationException('Template Map is immutable (for now)')
    }

    @Override
    Set<String> keySet() {
        reload()
        return templateIdMap.keySet()
    }

    @Override
    Collection<Object> values() {
        reload()
        return templateIdMap.values()
    }

    @Override
    Set<Map.Entry<String, Object>> entrySet() {
        reload()
        return templateIdMap.entrySet()
    }

    def leftShift(file) {
        importTemplate(file)
    }

    void importTemplate(file) {

        StringBody body = new StringBody((file as File).text)
        nifi.http.request(POST) { request ->
            uri.path = '/nifi-api/controller/templates'

            requestContentType = 'multipart/form-data'
            MultipartEntity entity = new MultipartEntity()
            entity.addPart("template", body)
            request.entity = entity

            response.success = { resp, xml ->
                switch (resp.statusLine.statusCode) {
                    case 200:
                        // TODO Template already exists, perhaps we can delete and re-import here
                        break
                    case 201:
                        // Template was created successfully
                        reload()
                        break

                    default:
                        // Something went wrong, throw an exception
                        throw new Exception("Error importing $file")
                        break
                }

            }
        }
    }


    def reload() {
        synchronized (this.templateIdMap) {
            this.templateIdMap.clear()
            def procs = slurper.parseText("${nifi.urlString}/nifi-api/controller/templates".toURL().text).templates
            def map = this.templateIdMap
            def n = this.nifi
            procs.each { p ->
                map.put(p.name, new Template(n, p))
            }
        }
    }
}
