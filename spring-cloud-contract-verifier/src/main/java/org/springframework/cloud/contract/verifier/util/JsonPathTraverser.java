/*
 * Copyright 2013-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.verifier.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

import groovy.lang.GString;

import org.springframework.cloud.contract.spec.internal.ExecutionProperty;
import org.springframework.cloud.contract.spec.internal.OptionalProperty;

/**
 * Traverses JSON structures and builds JSON path assertions. Handles both ordered (exact
 * index) and unordered (any matching) array verification.
 *
 * @author Marcin Grzejszczak
 * @since 5.1.0
 */
class JsonPathTraverser {

	private final boolean useOrderedArrayVerification;

	private final Function<String, Object> parsingFunction;

	JsonPathTraverser(boolean useOrderedArrayVerification, Function<String, Object> parsingFunction) {
		this.useOrderedArrayVerification = useOrderedArrayVerification;
		this.parsingFunction = parsingFunction;
	}

	/**
	 * Traverses the JSON starting from the root key and collects all verifiable paths.
	 * @param json the JSON to traverse
	 * @param rootKey the root verifiable key
	 * @param collector collects finished verifiable keys
	 */
	void traverse(Object json, MethodBufferingJsonVerifiable rootKey,
			Consumer<MethodBufferingJsonVerifiable> collector) {
		processValue(Map.class, rootKey, json, collector);
	}

	@SuppressWarnings("unchecked")
	private Object processValue(Class<?> parentType, MethodBufferingJsonVerifiable key, Object value,
			Consumer<MethodBufferingJsonVerifiable> collector) {
		value = ContentUtils.returnParsedObject(value);
		if (value instanceof String s && !s.isEmpty()) {
			return processString(key, s, collector);
		}
		if (value instanceof Map) {
			return processMap(key, (Map<Object, Object>) value, collector);
		}
		if (value instanceof List) {
			return processList(key, (List<?>) value, collector);
		}
		if (key.isIteratingOverArray()) {
			processValue(Object.class, key.arrayField().contains(ContentUtils.returnParsedObject(value)),
					ContentUtils.returnParsedObject(value), collector);
		}
		return emitValue(collector, key, value);
	}

	private Object processString(MethodBufferingJsonVerifiable key, String value,
			Consumer<MethodBufferingJsonVerifiable> collector) {
		try {
			Object parsed = this.parsingFunction.apply(value);
			if (parsed instanceof Map) {
				return processMap(key, castToMap(parsed), collector);
			}
		}
		catch (Exception ignore) {
			// Not JSON, treat as regular string
		}
		return emitValue(collector, key, value);
	}

	@SuppressWarnings("unchecked")
	private Object processMap(MethodBufferingJsonVerifiable key, Map<Object, Object> map,
			Consumer<MethodBufferingJsonVerifiable> collector) {
		if (map.isEmpty()) {
			return emitValue(collector, key.isEmpty(), map);
		}
		if (isSimpleEntryMap(map)) {
			return convertMapEntries(List.class, key, map, collector);
		}
		return convertMapEntries(Map.class, key, map, collector);
	}

	private Object processList(MethodBufferingJsonVerifiable key, List<?> list,
			Consumer<MethodBufferingJsonVerifiable> collector) {

		if (list.isEmpty()) {
			return emitValue(collector, key, list);
		}

		boolean isPrimitiveList = listContainsOnlyPrimitives(list);

		if (isPrimitiveList) {
			addSizeCheckIfEnabled(key, list, collector);
			return processPrimitiveList(key, list, collector);
		}

		return processComplexList(key, list, collector);
	}

	private Object processPrimitiveList(MethodBufferingJsonVerifiable key, List<?> list,
			Consumer<MethodBufferingJsonVerifiable> collector) {

		if (this.useOrderedArrayVerification) {
			for (int i = 0; i < list.size(); i++) {
				Object element = ContentUtils.returnParsedObject(list.get(i));
				MethodBufferingJsonVerifiable indexedKey = key.elementWithIndex(i);
				processValue(Object.class, valueToAsserter(indexedKey, element), element, collector);
			}
		}
		else {
			MethodBufferingJsonVerifiable arrayKey = key.arrayField();
			for (Object item : list) {
				Object element = ContentUtils.returnParsedObject(item);
				processValue(Object.class, valueToAsserter(arrayKey, element), element, collector);
			}
		}
		return list;
	}

	private Object processComplexList(MethodBufferingJsonVerifiable key, List<?> list,
			Consumer<MethodBufferingJsonVerifiable> collector) {

		addSizeCheckIfEnabled(key, list, collector);

		if (this.useOrderedArrayVerification) {
			for (int i = 0; i < list.size(); i++) {
				Object element = ContentUtils.returnParsedObject(list.get(i));
				MethodBufferingJsonVerifiable indexedKey = key.elementWithIndex(i);
				processValue(List.class, createListElementAsserter(indexedKey, element), element, collector);
			}
		}
		else {
			MethodBufferingJsonVerifiable arrayKey = createArrayAsserter(key, list);
			for (Object element : list) {
				Object parsed = ContentUtils.returnParsedObject(element);
				processValue(List.class, createListElementAsserter(arrayKey, parsed), parsed, collector);
			}
		}
		return list;
	}

	// ========== Map Entry Processing ==========

	private Map<Object, Object> convertMapEntries(Class<?> parentType, MethodBufferingJsonVerifiable parentKey,
			Map<Object, Object> map, Consumer<MethodBufferingJsonVerifiable> collector) {

		Map<Object, Object> result = new LinkedHashMap<>();
		for (Map.Entry<Object, Object> entry : map.entrySet()) {
			Object entryKey = entry.getKey();
			Object value = ContentUtils.returnParsedObject(entry.getValue());
			MethodBufferingJsonVerifiable verifiable = createKeyVerifiable(parentKey, entryKey, value);
			result.put(entry.getKey(), processValue(parentType, verifiable, value, collector));
		}
		return result;
	}

	private MethodBufferingJsonVerifiable createKeyVerifiable(MethodBufferingJsonVerifiable parentKey, Object entryKey,
			Object value) {
		if (value instanceof List) {
			return createListFieldVerifiable((List<?>) value, entryKey, parentKey);
		}
		// Use ShouldTraverse to ensure field() is used instead of contains()
		// This is needed because after elementWithIndex, isIteratingOverArray() is true
		// which would otherwise cause contains() to be used
		return parentKey.field(new ShouldTraverse(entryKey));
	}

	private MethodBufferingJsonVerifiable createListFieldVerifiable(List<?> list, Object entryKey,
			MethodBufferingJsonVerifiable parentKey) {
		if (list.isEmpty()) {
			return parentKey.array(entryKey).isEmpty();
		}
		return listContainsOnlyPrimitives(list) ? parentKey.arrayField(entryKey) : parentKey.array(entryKey);
	}

	// ========== Asserter Creation ==========

	private MethodBufferingJsonVerifiable createArrayAsserter(MethodBufferingJsonVerifiable key, List<?> list) {
		if (key.isIteratingOverNamelessArray()) {
			return key.array();
		}
		if (key.isIteratingOverArray() && isListOfLists(list)) {
			boolean allPrimitive = list.stream()
				.filter(item -> item instanceof List)
				.allMatch(item -> listContainsOnlyPrimitives((List<?>) item));
			return allPrimitive ? key.iterationPassingArray() : key.array();
		}
		if (key.isIteratingOverArray()) {
			return key.iterationPassingArray();
		}
		return key;
	}

	private MethodBufferingJsonVerifiable createListElementAsserter(MethodBufferingJsonVerifiable verifiable,
			Object element) {
		if (verifiable.isAssertingAValueInArray()) {
			Object parsed = ContentUtils.returnParsedObject(element);
			// Don't call contains on Map or List elements - let processValue recurse into
			// them
			if (parsed instanceof Map || parsed instanceof List) {
				return verifiable;
			}
			if (parsed instanceof Pattern) {
				return verifiable.matches(((Pattern) parsed).pattern());
			}
			return verifiable.contains(parsed);
		}
		if (element instanceof List && listContainsOnlyPrimitives((List<?>) element)) {
			return verifiable.array();
		}
		return verifiable;
	}

	MethodBufferingJsonVerifiable valueToAsserter(MethodBufferingJsonVerifiable key, Object value) {
		Object converted = ContentUtils.returnParsedObject(value);

		if (key instanceof FinishedDelegatingJsonVerifiable) {
			return key;
		}
		if (converted instanceof Pattern) {
			return key.matches(((Pattern) converted).pattern());
		}
		if (converted instanceof OptionalProperty) {
			return key.matches(((OptionalProperty) converted).optionalPattern());
		}
		if (converted instanceof GString) {
			return key.matches(RegexpBuilders.buildGStringRegexpForTestSide((GString) converted));
		}
		if (converted instanceof ExecutionProperty) {
			return key;
		}
		// Use specific overloads for Number and Boolean to preserve types
		if (converted instanceof Number) {
			return key.isEqualTo((Number) converted);
		}
		if (converted instanceof Boolean) {
			return key.isEqualTo((Boolean) converted);
		}
		return key.isEqualTo(converted);
	}

	// ========== Size Verification ==========

	private void addSizeCheckIfEnabled(MethodBufferingJsonVerifiable key, List<?> list,
			Consumer<MethodBufferingJsonVerifiable> collector) {
		if (!this.useOrderedArrayVerification) {
			return;
		}
		if (!listContainsOnlyPrimitives(list)) {
			return;
		}
		if ((isRootElement(key) || key.assertsConcreteValue()) && !list.isEmpty()) {
			collector.accept((MethodBufferingJsonVerifiable) key.hasSize(list.size()));
		}
	}

	// ========== Emit Value ==========

	private Object emitValue(Consumer<MethodBufferingJsonVerifiable> collector, MethodBufferingJsonVerifiable key,
			Object value) {
		boolean isCollection = value instanceof List || value instanceof Map;

		if (key.isAssertingAValueInArray() && !isCollection) {
			collector.accept(valueToAsserter(key, value));
		}
		else if (isCollection || key instanceof FinishedDelegatingJsonVerifiable) {
			// For collections or already-finished keys, emit as-is
			collector.accept(key);
		}
		else {
			// For primitive values with non-finished keys, add equality assertion
			collector.accept(valueToAsserter(key, value));
		}
		return value;
	}

	// ========== Helper Methods ==========

	private boolean isRootElement(MethodBufferingJsonVerifiable key) {
		return "$".equals(key.jsonPath());
	}

	private boolean listContainsOnlyPrimitives(List<?> list) {
		if (list.isEmpty()) {
			return false;
		}
		for (Object element : list) {
			if (element == null || !isPrimitive(element)) {
				return false;
			}
		}
		return true;
	}

	private boolean isPrimitive(Object obj) {
		Class<?> clazz = obj.getClass();
		return String.class.isAssignableFrom(clazz) || Number.class.isAssignableFrom(clazz)
				|| Boolean.class.isAssignableFrom(clazz);
	}

	private boolean isSimpleEntryMap(Map<?, ?> map) {
		if (map.isEmpty()) {
			return false;
		}
		for (Object value : map.values()) {
			if (value == null || !isPrimitive(value)) {
				return false;
			}
		}
		return true;
	}

	private boolean isListOfLists(List<?> list) {
		for (Object item : list) {
			if (!(item instanceof List)) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private Map<Object, Object> castToMap(Object obj) {
		return (Map<Object, Object>) obj;
	}

}
