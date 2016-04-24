package io.codearte.accurest

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TypeChecked
import io.codearte.accurest.dsl.GroovyDsl

/**
 * A better name for GroovyDsl
 *
 * @author Marcin Grzejszczak
 */
@TypeChecked
@EqualsAndHashCode
@ToString(includeFields = true, includePackage = false, includeNames = true, includeSuper = true)
class Accurest extends GroovyDsl {
}
