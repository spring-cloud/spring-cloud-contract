package io.codearte.accurest.util

import com.toomuchcoding.jsonassert.JsonAssertion
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.codearte.accurest.dsl.internal.ExecutionProperty
import io.codearte.accurest.dsl.internal.OptionalProperty

import java.util.regex.Pattern
/**
 * @author Marcin Grzejszczak
 */
class JsonToJsonPathsConverter {

	private static final Boolean SERVER_SIDE = false
	private static final Boolean CLIENT_SIDE = true

	public static JsonPaths transformToJsonPathWithTestsSideValues(def json) {
		return transformToJsonPathWithValues(json, SERVER_SIDE)
	}

	public static JsonPaths transformToJsonPathWithStubsSideValues(def json) {
		return transformToJsonPathWithValues(json, CLIENT_SIDE)
	}

	private static JsonPaths transformToJsonPathWithValues(def json, boolean clientSide) {
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

	protected static def traverseRecursively(Class parentType, MethodBufferingJsonVerifiable key, def value, Closure closure) {
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
			value.each {
				traverseRecursively(Object, key.arrayField().contains(it), it, closure)
			}
		} else if (value instanceof List) {
			MethodBufferingJsonVerifiable jsonPathVerifiable = createAsserterFromList(key, value)
			value.each { def element ->
				traverseRecursively(List, createAsserterFromListElement(jsonPathVerifiable, element),
						element, closure)
			}
			return value
		} else if (key.isIteratingOverArray()) {
			traverseRecursively(Object, key.arrayField().contains(value), value, closure)
		}
		try {
			return runClosure(closure, key, value)
		} catch (Exception ignore) {
			return value
		}
	}

	private static MethodBufferingJsonVerifiable createAsserterFromList(MethodBufferingJsonVerifiable key, List value) {
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

	private static MethodBufferingJsonVerifiable createAsserterFromListElement(MethodBufferingJsonVerifiable jsonPathVerifiable, def element) {
		if (jsonPathVerifiable.isAssertingAValueInArray()) {
			def object = ContentUtils.returnParsedObject(element)
			if (object instanceof Pattern) {
				return jsonPathVerifiable.matches((object as Pattern).pattern())
			}
			return jsonPathVerifiable.contains(object)
		}
		return jsonPathVerifiable
	}

	private static def runClosure(Closure closure, MethodBufferingJsonVerifiable key, def value) {
		if (key.isAssertingAValueInArray()) {
			return closure(valueToAsserter(key, value), value)
		}
		return closure(key, value)
	}

	private static boolean isAnEntryWithNonCollectionLikeValue(def value) {
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

	private static boolean isAnEntryWithoutNestedStructures(def value) {
		if (!(value instanceof Map)) {
			return false
		}
		Map valueAsMap = ((Map) value)
		return valueAsMap.entrySet().every { Map.Entry entry ->
			[String, Number, Boolean].any { it.isAssignableFrom(entry.value.getClass()) }
		}
	}

	private static boolean listContainsOnlyPrimitives(List list) {
		return list.every { def element ->
			[String, Number, Boolean].any {
				it.isAssignableFrom(element.getClass())
			}
		}
	}
	private static boolean isAnEntryWithLists(def value) {
		if (!(value instanceof Iterable)) {
			return false
		}
		return value.every { def entry ->
			entry instanceof List
		}
	}

	private static Map convertWithKey(Class parentType, MethodBufferingJsonVerifiable parentKey, Map map, Closure closureToExecute) {
		return map.collectEntries {
			Object entrykey, value ->
				def convertedValue = ContentUtils.returnParsedObject(value)
				[entrykey, traverseRecursively(parentType,
							convertedValue instanceof List ? listContainsOnlyPrimitives(convertedValue) ?
									parentKey.arrayField(entrykey) :
									parentKey.array(entrykey) :
							convertedValue instanceof Map ? parentKey.field(new ShouldTraverse(entrykey)) :
									valueToAsserter(parentKey.field(entrykey), convertedValue)
							, convertedValue, closureToExecute)]
		}
	}

	private static void traverseRecursivelyForKey(def json, MethodBufferingJsonVerifiable rootKey, Closure closure) {
		traverseRecursively(Map, rootKey, json, closure)
	}

	protected static MethodBufferingJsonVerifiable valueToAsserter(MethodBufferingJsonVerifiable key, Object value) {
		if (key instanceof FinishedDelegatingJsonVerifiable) {
			return key
		}
		if (value instanceof Pattern) {
			return key.matches((value as Pattern).pattern())
		} else if (value instanceof OptionalProperty) {
			return key.matches((value as OptionalProperty).optionalPattern())
		} else if (value instanceof GString) {
			return key.matches(RegexpBuilders.buildGStringRegexpForTestSide(value))
		} else if (ContentUtils.returnParsedObject(value) instanceof  ExecutionProperty) {
			return key
		}
		return key.isEqualTo(value)
	}

}
