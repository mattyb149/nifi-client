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

import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import static junit.framework.Assert.assertEquals

/**
 * Created by mburgess on 12/30/15.
 */
class NiFiTest {
    private static final String URL = 'http://127.0.0.1:8080'

    @Before
    void setUp() {

    }

    @After
    void tearDown() {

    }

    @Test
    void testBind() {
        NiFi nifi = NiFi.bind(URL)
        assertEquals(URL, nifi.urlString)
    }

    @Test
    void testGetCurrentVersion() {
        // TODO define a mock client
        NiFi nifi = NiFi.bind(URL)
        //assertEquals(1L, nifi.currentVersion)
    }

    @Test
    @Ignore('Comment out this line if you want to run against a local NiFi instance')
    void testGetProcessors() {
        NiFi nifi = NiFi.bind(URL)

        println '\nProcessors\n-------------------'
        nifi.processors.each { k, v -> println "$k (${v.id}) is ${v.state}" }

        println '\nOriginal Templates\n-------------------'
        nifi.templates.each { k, v -> println "$k (${v.id})" }

        nifi.templates << "/Users/mburgess/datasets/GetUserData.xml"

        println '\nTemplates\n-------------------'
        nifi.templates.each { k, v -> println "$k (${v.id})"
        }

        nifi.templates.'GetUserData'.instantiate()

        nifi.processors.types().each { searchType ->
            println "\nType: $searchType"
            nifi.processors.findByType(searchType).each { println "\t${it.name}"}
        }

        println '\nFinal Processors\n-------------------'
        nifi.processors.each { k, v -> println "$k (${v.id}) is ${v.state}" }

        nifi.templates.'GetUserData'.delete()

        println '\nFinal Templates\n-------------------'
        nifi.templates.each { k, v -> println "$k (${v.id})"
        }
    }
}
