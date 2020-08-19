/*
 * Copyright 2013-2020 the original author or authors.
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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import org.springframework.util.ReflectionUtils;

/**
 * Helper class that allows to work with metadata.
 *
 * @author Marcin Grzejszczak
 * @since 3.0.0
 */
public final class MetadataUtil {

	private static final ObjectMapper MAPPER = new VerifierObjectMapper();

	private MetadataUtil() {
		throw new IllegalStateException("Can't instantiate a utility class");
	}

	/**
	 * Fills an object with entries from metadata for a given key.
	 * @param metadata - metadata from the contract
	 * @param key - key under which metadata can be found
	 * @param objectToMerge - object to be filled with entries from the metadata
	 * @param <T> - type of the object to merge
	 * @return merged object with metadata or object without metadata entries if metadata
	 * key wasn't present
	 */
	public static <T> T fromMetadata(Map<String, Object> metadata, String key,
			T objectToMerge) {
		if (!metadata.containsKey(key)) {
			return objectToMerge;
		}
		return merge(objectToMerge, metadata.get(key));
	}

	/**
	 * Patches the object to merge.
	 * @param objectToMerge - object to be filled with entries from the metadata
	 * @param patch - object that is a patch for an object to merge
	 * @param <T> - type of the object to merge
	 * @return merged object with metadata or object without metadata entries if metadata
	 * key wasn't present
	 */
	public static <T> T merge(T objectToMerge, Object patch) {
		if (patch == null) {
			return objectToMerge;
		}
		try {
			byte[] bytes = MAPPER.writer().writeValueAsBytes(patch);
			return MAPPER.readerForUpdating(objectToMerge).readValue(bytes);
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public static MetadataMap map() {
		return new MetadataMap();
	}

	public static class MetadataMap implements Map<String, Object> {

		private final Map<String, Object> delegate = new HashMap<>();

		public MetadataMap entry(String key, Object value) {
			put(key, value);
			return this;
		}

		@Override
		public int size() {
			return this.delegate.size();
		}

		@Override
		public boolean isEmpty() {
			return this.delegate.isEmpty();
		}

		@Override
		public boolean containsKey(Object key) {
			return this.delegate.containsKey(key);
		}

		@Override
		public boolean containsValue(Object value) {
			return this.delegate.containsValue(value);
		}

		@Override
		public Object get(Object key) {
			return this.delegate.get(key);
		}

		@Override
		public Object put(String key, Object value) {
			return this.delegate.put(key, value);
		}

		@Override
		public Object remove(Object key) {
			return this.delegate.remove(key);
		}

		@Override
		public void putAll(Map<? extends String, ?> m) {
			this.delegate.putAll(m);
		}

		@Override
		public void clear() {
			this.delegate.clear();
		}

		@Override
		public Set<String> keySet() {
			return this.delegate.keySet();
		}

		@Override
		public Collection<Object> values() {
			return this.delegate.values();
		}

		@Override
		public Set<Entry<String, Object>> entrySet() {
			return this.delegate.entrySet();
		}

		@Override
		public boolean equals(Object o) {
			return this.delegate.equals(o);
		}

		@Override
		public int hashCode() {
			return this.delegate.hashCode();
		}

	}

}

class VerifierObjectMapper extends ObjectMapper {

	VerifierObjectMapper() {
		setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
				.setDefaultPropertyInclusion(JsonInclude.Include.NON_DEFAULT)
				.setDefaultPropertyInclusion(JsonInclude.Include.NON_EMPTY)
				.setDefaultPropertyInclusion(JsonInclude.Include.NON_ABSENT);
		configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		FilterProvider filters = new SimpleFilterProvider()
				.addFilter("non default properties", new MyFilter());
		addMixIn(Object.class, PropertyFilterMixIn.class);
		setFilterProvider(filters);
	}

}

@JsonFilter("non default properties")
class PropertyFilterMixIn {

}

class MyFilter extends SimpleBeanPropertyFilter implements Serializable {

	private static final Map<Class, Object> CACHE = new ConcurrentHashMap<>();

	@Override
	public void serializeAsField(Object pojo, JsonGenerator jgen,
			SerializerProvider provider, PropertyWriter writer) throws Exception {
		if (pojo instanceof Map || pojo instanceof Collection) {
			writer.serializeAsField(pojo, jgen, provider);
			return;
		}
		Object defaultInstance = CACHE.computeIfAbsent(pojo.getClass(),
				this::defaultInstance);
		if (defaultInstance instanceof CantInstantiateThisClass
				|| !valueSameAsDefault(pojo, defaultInstance, writer.getName())) {
			writer.serializeAsField(pojo, jgen, provider);
		}
	}

	private Object defaultInstance(Class aClass) {
		try {
			return aClass.newInstance();
		}
		catch (Exception e) {
			return new CantInstantiateThisClass();
		}
	}

	private boolean valueSameAsDefault(Object pojo, Object defaultInstance,
			String fieldName) {
		Field field = ReflectionUtils.findField(pojo.getClass(), fieldName);
		if (field == null) {
			return false;
		}
		ReflectionUtils.makeAccessible(field);
		try {
			return Objects.equals(field.get(pojo), field.get(defaultInstance));
		}
		catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

}

class CantInstantiateThisClass {

}
