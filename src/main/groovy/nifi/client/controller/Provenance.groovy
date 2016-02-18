package nifi.client.controller

import groovy.json.JsonSlurper
import nifi.client.NiFi

import static groovyx.net.http.ContentType.URLENC
import static groovyx.net.http.Method.DELETE
import static groovyx.net.http.Method.POST

/**
 * Created by mburgess on 2/18/16.
 */
class Provenance {
    NiFi nifi
    List<ProvenanceEvent> events = []

    private static final JsonSlurper slurper = new JsonSlurper()

    protected Provenance(NiFi nifi) {
        this.nifi = nifi
    }

    def get(Map queryParams) {

        // Need to get latest revision
        def latestVersion = nifi.currentVersion
        def options = [version   : latestVersion] << queryParams
        try {
            String retryUrl
            nifi.http.request(POST) {
                uri.path = '/nifi-api/controller/provenance'
                send URLENC, options

                response.success = { instanceResp, json ->
                    retryUrl = parseResponse(json)

                    while (retryUrl) {
                        Thread.sleep(250)
                        retryUrl = parseResponse(slurper.parseText(retryUrl.toURL().text))
                    }
                }
                response.'404' = { throw new Exception("Couldn't find template with ID $tID") }
            }
        } catch (e) {
            e.printStackTrace(System.err)
        }
        events
    }

    private String parseResponse(json) {
        def n = this.nifi
        def responseId = json.provenance.id
        // Check for completion
        if(json.provenance.finished) {
            json.provenance.results.provenanceEvents.each { events.add(new ProvenanceEvent(n, it as Map)) }
            nifi.http.request(DELETE) { uri.path="/nifi-api/controller/provenance/$responseId" }
            return null
        } else {
            return json.provenance.uri
        }
    }
}
