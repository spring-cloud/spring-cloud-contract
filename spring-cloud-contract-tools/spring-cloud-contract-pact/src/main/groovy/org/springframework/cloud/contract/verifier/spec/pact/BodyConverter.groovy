/*
 *  Copyright 2013-2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.springframework.cloud.contract.verifier.spec.pact

import au.com.dius.pact.consumer.dsl.DslPart
import au.com.dius.pact.consumer.dsl.PactDslJsonArray
import au.com.dius.pact.consumer.dsl.PactDslJsonBody
import au.com.dius.pact.model.OptionalBody
import au.com.dius.pact.model.Request
import au.com.dius.pact.model.Response
import au.com.dius.pact.model.generators.Generator
import au.com.dius.pact.model.v3.messaging.Message
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.internal.EvaluationContext
import com.jayway.jsonpath.internal.Path
import com.jayway.jsonpath.internal.PathRef
import com.jayway.jsonpath.internal.path.PathCompiler
import groovy.json.JsonException
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.springframework.cloud.contract.spec.internal.Body
import org.springframework.cloud.contract.spec.internal.ClientDslProperty
import org.springframework.cloud.contract.spec.internal.DslProperty
import org.springframework.cloud.contract.spec.internal.ServerDslProperty
import org.springframework.cloud.contract.verifier.util.ContentUtils

import java.util.regex.Pattern

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
		return p
	}

	private static DslPart createRootDslPart(Object value) {
		return value instanceof Collection ? new PactDslJsonArray() : new PactDslJsonBody()
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

	static def toSCCBody(Request request) {
		def body = parseBody(request.body)
		if (request.generators.isNotEmpty() && request.generators.categories.containsKey(au.com.dius.pact.model.generators.Category.BODY)) {
			applyGenerators(body, request.generators.categories.get(au.com.dius.pact.model.generators.Category.BODY)) { Object currentValue, Pattern pattern, Object generatedValue ->
				return new DslProperty<Object>(new ClientDslProperty(pattern, generatedValue), currentValue)
			}
		}
		return body
	}

	static def toSCCBody(Response response) {
		def body = parseBody(response.body)
		if (response.generators.isNotEmpty() && response.generators.categories.containsKey(au.com.dius.pact.model.generators.Category.BODY)) {
			applyGenerators(body, response.generators.categories.get(au.com.dius.pact.model.generators.Category.BODY)) { Object currentValue, Pattern pattern, Object generatedValue ->
				return new DslProperty<Object>(currentValue, new ServerDslProperty(pattern, generatedValue))
			}
		}
		return body
	}

	static def toSCCBody(Message message) {
		def body = parseBody(message.contents)
		if (message.generators.isNotEmpty() && message.generators.categories.containsKey(au.com.dius.pact.model.generators.Category.BODY)) {
			applyGenerators(body, message.generators.categories.get(au.com.dius.pact.model.generators.Category.BODY)) { Object currentValue, Pattern pattern, Object generatedValue ->
				return new DslProperty<Object>(new ClientDslProperty(pattern, generatedValue), currentValue)
			}
		}
		return body
	}

	private static def parseBody(OptionalBody optionalBody) {
		if (optionalBody.present) {
			return new JsonSlurper().parseText(optionalBody.value)
		} else {
			return optionalBody.value
		}
	}

	private static void applyGenerators(def body, Map<String, Generator> generatorsPerPath, Closure<DslProperty> dslPropertyProvider) {
		Configuration configuration = Configuration.defaultConfiguration()
		generatorsPerPath.each { String path, Generator generator ->
			Path compiledPath = PathCompiler.compile(path)
			EvaluationContext evaluationContext = compiledPath.evaluate(body, body, configuration, true)
			evaluationContext.updateOperations().each { PathRef pathRef ->
				pathRef.convert({ Object currentValue, Configuration config ->
					return ValueGeneratorConverter.convert(generator) { Pattern pattern, Object generatedValue ->
						return dslPropertyProvider(currentValue, pattern, generatedValue)
					}
				}, configuration)
			}
		}
	}

}
