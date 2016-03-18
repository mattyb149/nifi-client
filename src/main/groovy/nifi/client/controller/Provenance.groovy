package nifi.client.controller

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import nifi.client.NiFi

import static groovyx.net.http.ContentType.URLENC
import static groovyx.net.http.Method.DELETE
import static groovyx.net.http.Method.POST

/**
 * An object corresponding to the provenance model
 */
class Provenance {
    NiFi nifi
    List<ProvenanceEvent> events = null

    private static final JsonSlurper slurper = new JsonSlurper()

    protected Provenance(NiFi nifi) {
        this.nifi = nifi
    }

    def events(Map queryParams) {

        // Need to get latest revision
        def latestVersion = nifi.currentVersion
        if (latestVersion == -1L) throw new Exception("Bad version")
        def options = [version: latestVersion] << queryParams
        try {
            String retryUrl
            nifi.http.request(POST) {
                uri.path = '/nifi-api/controller/provenance'
                send URLENC, options

                response.success = { instanceResp, json ->
                    try {
                        events = []
                        retryUrl = parseResponse(json)

                        while (retryUrl) {
                            Thread.sleep(500)
                            retryUrl = parseResponse(slurper.parseText(retryUrl.toURL().text))
                        }
                    } catch (Exception e) {
                        nifi.http.request(DELETE) {
                            uri.path = "/nifi-api/controller/provenance/${json.provenance.id}"
                        }
                    }
                }
                response.'404' = { throw new Exception("Couldn't find template with ID $tID") }
                response.'409' = { instanceResp, json -> println "Conflict"; System.out << json; }
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
        if (json.provenance.finished) {
            json.provenance.results.provenanceEvents.each { events.add(new ProvenanceEvent(n, it as Map)) }
            nifi.http.request(DELETE) {
                uri.path = "/nifi-api/controller/provenance/$responseId"
            }
            return null
        } else {
            return json.provenance.uri
        }
    }

    def rightShift(file) {
        (file as File).withWriter { out ->
            if (events?.isEmpty()) throw new IllegalArgumentException("No events to write")

            def eventNodeIds = []
            events.each { event ->
                def lineage = event.lineage()
                def links = lineage.links.collect { ['outV': it.targetId, 'inV': it.sourceId] }
                def builder = new JsonBuilder()
                lineage.nodes.each { node ->
                    if (!eventNodeIds.contains(node.id)) {
                        builder.call {
                            id node.id
                            eventNodeIds.add(node.id)

                            label node.eventType?.toLowerCase() ?: node.type.toLowerCase()
                            def myInE = links.findResults {
                                it.outV == node.id ? ['id': UUID.randomUUID(), 'outV': it.inV] : null
                            }
                            if (myInE && !myInE.isEmpty()) {
                                inE {
                                    then myInE
                                }
                            }
                            def myOutE = links.findResults {
                                it.inV == node.id ? ['id': UUID.randomUUID(), 'inV': it.outV] : null
                            }
                            if (myOutE && !myOutE.isEmpty()) {
                                outE {
                                    then myOutE
                                }
                            }
                            properties {
                                'provenanceNodeType'([['id': UUID.randomUUID(), 'value': node.type]] as List)
                                'flowFileUuid'([['id': UUID.randomUUID(), 'value': node.flowFileUuid]] as List)
                                'timestamp'([['id': UUID.randomUUID(), 'value': node.timestamp]] as List)
                                'millis'([['id': UUID.randomUUID(), 'value': node.millis]] as List)
                            }
                        }

                        out.writeLine(builder.toString())
                    }
                }
            }
        }
        file
    }
}
