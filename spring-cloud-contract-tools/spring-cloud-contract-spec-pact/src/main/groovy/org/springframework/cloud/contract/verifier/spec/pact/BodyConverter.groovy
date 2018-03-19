package org.springframework.cloud.contract.verifier.spec.pact

import au.com.dius.pact.consumer.dsl.DslPart
import au.com.dius.pact.consumer.dsl.PactDslJsonArray
import au.com.dius.pact.consumer.dsl.PactDslJsonBody
import groovy.json.JsonException
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.springframework.cloud.contract.spec.internal.Body
import org.springframework.cloud.contract.spec.internal.DslProperty
import org.springframework.cloud.contract.verifier.util.ContentUtils

/**
 * @author Tim Ysewyn
 * @Since 2.0.0
 */
@CompileStatic
@PackageScope
class BodyConverter {

	private static final JsonSlurper jsonSlurper = new JsonSlurper()

	static DslPart toPactBody(Body body, Closure dslPropertyValueExtractor) {
		return traverse(body, null, dslPropertyValueExtractor)
	}

	static DslPart toPactBody(DslProperty dslProperty, Closure dslPropertyValueExtractor) {
		return traverse(dslProperty, null, dslPropertyValueExtractor)
	}

	private static DslPart traverse(Object value, DslPart parent, Closure dslPropertyValueExtractor) {
		boolean isRoot = parent == null
		Object v = value

		if (v instanceof DslProperty) {
			v = dslPropertyValueExtractor(v)
		}

		if (v instanceof GString) {
			v = ContentUtils.extractValue(v, dslPropertyValueExtractor)
		}

		if (v instanceof String) {
			v = v.trim()
			if (v.startsWith("{") && v.endsWith("}")) {
				try {
					v = jsonSlurper.parseText(v as String)
				} catch (JsonException ex) { /*it wasn't a JSON string after all...*/
				}
			}
		}

		DslPart p = isRoot ? createRootDslPart(v) : parent

		if (v instanceof Map) {
			if (!isRoot) {
				p = p.object()
			}
			processMap(v as Map, p as PactDslJsonBody, dslPropertyValueExtractor)
			if (!isRoot) {
				p = p.closeObject()
			}
		} else if (v instanceof Collection) {
			if (!isRoot) {
				p = p.array()
			}
			processCollection(v as Collection, p as PactDslJsonArray, dslPropertyValueExtractor)
			if (!isRoot) {
				p = p.closeArray()
			}
		}

		p
	}

	private static DslPart createRootDslPart(Object value) {
		value instanceof Collection ? new PactDslJsonArray() : new PactDslJsonBody()
	}

	private static void processCollection(Collection values, PactDslJsonArray jsonArray, Closure dslPropertyValueExtractor) {
		values.forEach({
			Object v = it

			if (v instanceof DslProperty) {
				v = dslPropertyValueExtractor(v)
			}

			if (v instanceof GString) {
				v = ContentUtils.extractValue(v, dslPropertyValueExtractor)
			}

			if (v == null) {
				jsonArray.nullValue()
			} else if (v instanceof String) {
				jsonArray.string(v)
			} else if (v instanceof Number) {
				jsonArray.number(v)
			} else {
				traverse(it, jsonArray, dslPropertyValueExtractor)
			}
		})
	}

	private static void processMap(Map<String, Object> values, PactDslJsonBody jsonObject, Closure dslPropertyValueExtractor) {
		values.forEach({ String k, Object v ->

			if (v instanceof DslProperty) {
				v = dslPropertyValueExtractor(v)
			}

			if (v instanceof GString) {
				v = ContentUtils.extractValue(v, dslPropertyValueExtractor)
			}

			if (v == null) {
				jsonObject.nullValue(k)
			} else if (v instanceof String) {
				jsonObject.stringType(k, v)
			} else if (v instanceof Number) {
				jsonObject.numberValue(k, v)
			} else {
				PactDslJsonBody current = jsonObject.object(k)
				traverse(v, current, dslPropertyValueExtractor)
				current.closeObject()
			}
		})
	}

}
