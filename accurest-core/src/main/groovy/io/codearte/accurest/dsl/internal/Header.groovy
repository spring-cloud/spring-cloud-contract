package io.codearte.accurest.dsl.internal
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode(includeFields = true)
@ToString(includePackage = false, includeFields = true, ignoreNulls = true, includeNames = true)
@CompileStatic
class Header extends DslProperty {

    String name

    Header(String name, DslProperty dslProperty) {
        super(dslProperty.clientValue, dslProperty.serverValue)
        this.name = name
    }

    Header(String name, Object value) {
        super(value)
        this.name = name
    }

}
