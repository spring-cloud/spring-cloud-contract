/*
 *  Copyright 2013-2017 the original author or authors.
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

package org.springframework.cloud.contract.verifier.util

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import com.toomuchcoding.jsonassert.JsonAssertion
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.springframework.cloud.contract.spec.internal.*
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties

import java.util.regex.Pattern

/**
 * I would like to apologize to anyone who is reading this class. Since JSON is a hectic structure
 * this class is also hectic. The idea is to traverse the JSON structure and build a set of
 * JSON Paths together with methods needed to be called to build them.
 *
 * @author Marcin Grzejszczak
 */
@Slf4j
class JsonToJsonPathsConverter {

	/**
	 * In case of issues with size assertion just provide this property as system property
	 * equal to "false" and then size assertion will be disabled
	 */
	private static final String SIZE_ASSERTION_SYSTEM_PROP = "spring.cloud.contract.verifier.assert.size"

	private static final Boolean SERVER_SIDE = false
	private static final Boolean CLIENT_SIDE = true

	private final ContractVerifierConfigProperties configProperties

	JsonToJsonPathsConverter(ContractVerifierConfigProperties configProperties) {
		this.configProperties = configProperties
	}

	JsonToJsonPathsConverter() {
		this.configProperties = new ContractVerifierConfigProperties()
		if (log.isDebugEnabled()) {
			log.debug("Creating JsonToJsonPaths converter with default properties")
		}
	}

	/**
	 * Removes from the parsed json any JSON path matching entries.
	 * That way we remain with values that should be checked in the auto-generated
	 * fashion.
	 *
	 * @param json - parsed JSON
	 * @param bodyMatchers - the part of request / response that contains matchers
	 * @return json with removed entries
	 */
	static def removeMatchingJsonPaths(def json, BodyMatchers bodyMatchers) {
		def jsonCopy = json.clone()
		DocumentContext context = JsonPath.parse(jsonCopy)
		if (bodyMatchers?.hasMatchers()) {
			bodyMatchers.jsonPathMatchers().each { BodyMatcher matcher ->
				context.delete(matcher.path())
			}
		}
		return jsonCopy
	}

	/**
	 * For the given matcher converts it into a JSON path
	 * that checks the regex pattern or equality
	 *
	 * @param bodyMatcher
	 * @return JSON path that checks the regex for its last element
	 */
	static String convertJsonPathAndRegexToAJsonPath(BodyMatcher bodyMatcher, def body = null) {
		String path = bodyMatcher.path()
		Object value = bodyMatcher.value()
		if (value == null && bodyMatcher.matchingType() != MatchingType.EQUALITY) {
			return path
		}
		int lastIndexOfDot = path.lastIndexOf(".")
		String toLastDot = path.substring(0, lastIndexOfDot)
		String fromLastDot = path.substring(lastIndexOfDot + 1)
		String comparison = createComparison(bodyMatcher, value, body)
		return "${toLastDot}[?(@.${fromLastDot} ${comparison})]"
	}

	private static String createComparison(BodyMatcher bodyMatcher, Object value, def body) {
		if (bodyMatcher.matchingType() == MatchingType.EQUALITY) {
			if (!body) {
				throw new IllegalStateException("Body hasn't been passed")
			}
			try {
				Object retrievedValue = JsonPath.parse(body).read(bodyMatcher.path())
				String wrappedValue = retrievedValue instanceof Number ? retrievedValue : "'${retrievedValue.toString()}'"
				return "== ${wrappedValue}"
			} catch (PathNotFoundException e) {
				throw new IllegalStateException("Value [${bodyMatcher.path()}] not found in JSON [${JsonOutput.toJson(body)}]", e)
			}
		} else {
			return "=~ /(${value})/"
		}
	}

	JsonPaths transformToJsonPathWithTestsSideValues(def json) {
		return transformToJsonPathWithValues(json, SERVER_SIDE)
	}

	JsonPaths transformToJsonPathWithStubsSideValues(def json) {
		return transformToJsonPathWithValues(json, CLIENT_SIDE)
	}

	static JsonPaths transformToJsonPathWithStubsSideValuesAndNoArraySizeCheck(def json) {
		return new JsonToJsonPathsConverter()
				.transformToJsonPathWithValues(json, CLIENT_SIDE)
	}

	private JsonPaths transformToJsonPathWithValues(def json, boolean clientSide) {
		if(!json) {
			return new JsonPaths()
		}
		JsonPaths pathsAndValues = [] as Set
		Object convertedJson = MapConverter.getClientOrServerSideValues(json, clientSide)
		Object jsonWithPatterns = ContentUtils.convertDslPropsToTemporaryRegexPatterns(convertedJson)
		MethodBufferingJsonVerifiable methodBufferingJsonPathVerifiable =
				new DelegatingJsonVerifiable(JsonAssertion.assertThat(JsonOutput.toJson(jsonWithPatterns)).withoutThrowingException())
		traverseRecursivelyForKey(jsonWithPatterns, methodBufferingJsonPathVerifiable)
				 { MethodBufferingJsonVerifiable key, Object value ->
			if (value instanceof ExecutionProperty || !(key instanceof FinishedDelegatingJsonVerifiable)) {
				return
			}
			pathsAndValues.add(key)
		}
		return pathsAndValues
	}

	protected def traverseRecursively(Class parentType, MethodBufferingJsonVerifiable key, def value, Closure closure) {
		value = ContentUtils.returnParsedObject(value)
		if (value instanceof String && value) {
			try {
				def json = new JsonSlurper().parseText(value)
				if (json instanceof Map) {
					return convertWithKey(parentType, key, json, closure)
				}
			} catch (Exception ignore) {
				return runClosure(closure, key, value)
			}
		} else if (isAnEntryWithNonCollectionLikeValue(value)) {
			return convertWithKey(List, key, value as Map, closure)
		} else if (isAnEntryWithoutNestedStructures(value)) {
			return convertWithKey(List, key, value as Map, closure)
		} else if (value instanceof Map) {
			return convertWithKey(Map, key, value as Map, closure)
			// JSON with a list of primitives ["a", "b", "c"] in root issue #266
		} else if (key.isIteratingOverNamelessArray() && value instanceof List && listContainsOnlyPrimitives(value)) {
			addSizeVerificationForListWithPrimitives(key, closure, value)
			value.each {
				traverseRecursively(Object, key.arrayField().contains(ContentUtils.returnParsedObject(it)),
						ContentUtils.returnParsedObject(it), closure)
			}
		// JSON containing list of primitives { "partners":[ { "role":"AGENT", "payment_methods":[ "BANK", "CASH" ]	} ]
		} else if (value instanceof List && listContainsOnlyPrimitives(value)) {
			addSizeVerificationForListWithPrimitives(key, closure, value)
			value.each {
				traverseRecursively(Object, valueToAsserter(key.arrayField(), ContentUtils.returnParsedObject(it)),
						ContentUtils.returnParsedObject(it), closure)
			}
		} else if (value instanceof List && !value.empty) {
			MethodBufferingJsonVerifiable jsonPathVerifiable = createAsserterFromList(key, value)
			addSizeVerificationForListWithPrimitives(key, closure, value)
			value.each { def element ->
				traverseRecursively(List, createAsserterFromListElement(jsonPathVerifiable, ContentUtils.returnParsedObject(element)),
						ContentUtils.returnParsedObject(element), closure)
			}
			return value
		}  else if (value instanceof List && value.empty) {
			return runClosure(closure, key, value)
		}  else if (key.isIteratingOverArray()) {
			traverseRecursively(Object, key.arrayField().contains(ContentUtils.returnParsedObject(value)),
					ContentUtils.returnParsedObject(value), closure)
		}
		try {
			return runClosure(closure, key, value)
		} catch (Exception ignore) {
			return value
		}
	}

	// Size verification: https://github.com/Codearte/accurest/issues/279
	private void addSizeVerificationForListWithPrimitives(MethodBufferingJsonVerifiable key, Closure closure, List value) {
		String systemPropValue = System.getProperty(SIZE_ASSERTION_SYSTEM_PROP)
		Boolean configPropValue = configProperties.assertJsonSize
		if ((systemPropValue != null && Boolean.parseBoolean(systemPropValue)) ||
				configPropValue) {
			addArraySizeCheck(key, value, closure)
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Turning off the incubating feature of JSON array check. " +
						"System property [$systemPropValue]. Config property [$configPropValue]")
			}
			return
		}
	}

	private void addArraySizeCheck(MethodBufferingJsonVerifiable key, List value, Closure closure) {
		if (log.isDebugEnabled()) {
			log.debug("WARNING: Turning on the incubating feature of JSON array check")
		}
		if (isRootElement(key) || key.assertsConcreteValue()) {
			if (value.size() > 0) {
				closure(key.hasSize(value.size()), value)
			}
		}
	}

	private boolean isRootElement(MethodBufferingJsonVerifiable key) {
		return key.jsonPath() == '$'
	}

	// If you have a list of not-only primitives it can contain different sets of elements (maps, lists, primitives)
	private MethodBufferingJsonVerifiable createAsserterFromList(MethodBufferingJsonVerifiable key, List value) {
		if (key.isIteratingOverNamelessArray()) {
			return key.array()
		} else if (key.isIteratingOverArray() && isAnEntryWithLists(value)) {
			if (!value.every { listContainsOnlyPrimitives(it as List)} ) {
				return key.array()
			} else {
				return key.iterationPassingArray()
			}
		} else if (key.isIteratingOverArray()) {
			return key.iterationPassingArray()
		}
		return key
	}

	private MethodBufferingJsonVerifiable createAsserterFromListElement(MethodBufferingJsonVerifiable jsonPathVerifiable, def element) {
		if (jsonPathVerifiable.isAssertingAValueInArray()) {
			def object = ContentUtils.returnParsedObject(element)
			if (object instanceof Pattern) {
				return jsonPathVerifiable.matches((object as Pattern).pattern())
			}
			return jsonPathVerifiable.contains(object)
		}
		return jsonPathVerifiable
	}

	private def runClosure(Closure closure, MethodBufferingJsonVerifiable key, def value) {
		if (key.isAssertingAValueInArray() && !(value instanceof List || value instanceof Map)) {
			return closure(valueToAsserter(key, value), value)
		}
		return closure(key, value)
	}

	private boolean isAnEntryWithNonCollectionLikeValue(def value) {
		if (!(value instanceof Map)) {
			return false
		}
		Map valueAsMap = ((Map) value)
		boolean mapHasOneEntry = valueAsMap.size() == 1
		if (!mapHasOneEntry) {
			return false
		}
		Object valueOfEntry = valueAsMap.entrySet().first().value
		return !(valueOfEntry instanceof Map || valueOfEntry instanceof List)
	}

	private boolean isAnEntryWithoutNestedStructures(def value) {
		if (!(value instanceof Map)) {
			return false
		}
		Map valueAsMap = ((Map) value)
		return valueAsMap.entrySet().every { Map.Entry entry ->
			[String, Number, Boolean].any { it.isAssignableFrom(entry.value.getClass()) }
		}
	}

	private boolean listContainsOnlyPrimitives(List list) {
		if (list.empty) {
			return false
		}
		return list.every { def element ->
			[String, Number, Boolean].any {
				it.isAssignableFrom(element.getClass())
			}
		}
	}
	private boolean isAnEntryWithLists(def value) {
		if (!(value instanceof Iterable)) {
			return false
		}
		return value.every { def entry ->
			entry instanceof List
		}
	}

	private Map convertWithKey(Class parentType, MethodBufferingJsonVerifiable parentKey, Map map, Closure closureToExecute) {
		return map.collectEntries {
			Object entrykey, value ->
				def convertedValue = ContentUtils.returnParsedObject(value)
				[entrykey, traverseRecursively(parentType,
							convertedValue instanceof List ? list(convertedValue, entrykey, parentKey) :
							convertedValue instanceof Map ? parentKey.field(new ShouldTraverse(entrykey)) :
									valueToAsserter(parentKey.field(entrykey), convertedValue)
							, convertedValue, closureToExecute)]
		}
	}

	protected MethodBufferingJsonVerifiable list(List convertedValue, Object entrykey, MethodBufferingJsonVerifiable parentKey) {
		if (convertedValue.empty) {
			return parentKey.array(entrykey).isEmpty()
		}
		return listContainsOnlyPrimitives(convertedValue) ?
				parentKey.arrayField(entrykey) :
				parentKey.array(entrykey)
	}

	private void traverseRecursivelyForKey(def json, MethodBufferingJsonVerifiable rootKey, Closure closure) {
		traverseRecursively(Map, rootKey, json, closure)
	}

	protected MethodBufferingJsonVerifiable valueToAsserter(MethodBufferingJsonVerifiable key, Object value) {
		def convertedValue = ContentUtils.returnParsedObject(value)
		if (key instanceof FinishedDelegatingJsonVerifiable) {
			return key
		}
		if (convertedValue instanceof Pattern) {
			return key.matches((convertedValue as Pattern).pattern())
		} else if (convertedValue instanceof OptionalProperty) {
			return key.matches((convertedValue as OptionalProperty).optionalPattern())
		} else if (convertedValue instanceof GString) {
			return key.matches(RegexpBuilders.buildGStringRegexpForTestSide(convertedValue))
		} else if (convertedValue instanceof  ExecutionProperty) {
			return key
		}
		return key.isEqualTo(convertedValue)
	}

}
