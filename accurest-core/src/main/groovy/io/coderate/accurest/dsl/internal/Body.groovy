package io.coderate.accurest.dsl.internal

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includePackage = false, includeFields = true)
@EqualsAndHashCode(includeFields = true)
class Body {

    private Map<String, DslProperty> body
    private DslProperty bodyAsValue
    private List<DslProperty> bodyAsList

    Body() {
        this.body = [:]
    }

    Body(Map<String, DslProperty> body) {
        this.body = body
    }

    Body(List bodyAsList) {
        this.bodyAsList = bodyAsList
    }

    Body(Object bodyAsValue) {
        this.bodyAsValue = new DslProperty(bodyAsValue)
    }

    Body(DslProperty bodyAsValue) {
        this.bodyAsValue = bodyAsValue
    }

    Object forClientSide() {
        if(bodyAsValue) {
            return bodyAsValue.clientValue
        } else if(bodyAsList) {
            bodyAsList.collect { it.clientValue }
        }
        return body.collectEntries {
            Map.Entry<String, DslProperty> entry -> [(entry.key) : entry.value.clientValue]
        } as Map<String, Object>
    }

    Object forServerSide() {
        if(bodyAsValue) {
            return bodyAsValue.serverValue
        } else if(bodyAsList) {
            bodyAsList.collect { it.serverValue }
        }
        return body.collectEntries {
            Map.Entry<String, DslProperty> entry -> [(entry.key) : entry.value.serverValue]
        } as Map<String, Object>
    }
}
