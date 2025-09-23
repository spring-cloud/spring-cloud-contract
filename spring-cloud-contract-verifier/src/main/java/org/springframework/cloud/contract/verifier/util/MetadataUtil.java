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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.exc.InvalidDefinitionException;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.ser.PropertyWriter;
import tools.jackson.databind.ser.std.SimpleBeanPropertyFilter;
import tools.jackson.databind.ser.std.SimpleFilterProvider;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.ReflectionUtils;

/**
 * Helper class that allows to work with metadata.
 *
 * @author Marcin Grzejszczak
 * @since 3.0.0
 */
public final class MetadataUtil {

	private static final JsonMapper MAPPER = buildJsonMapper();

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
	public static <T> T fromMetadata(Map<String, Object> metadata, String key, T objectToMerge) {
		if (metadata == null || !metadata.containsKey(key)) {
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
		byte[] bytes = new byte[0];
		try {
			bytes = MAPPER.writer().writeValueAsBytes(patch);
			return MAPPER.readerForUpdating(objectToMerge).readValue(bytes);
		}
		catch (Exception e) {
			if (e.getClass().toString().contains("InaccessibleObjectException")
					|| (e instanceof InvalidDefinitionException
							&& e.getMessage().contains("InaccessibleObjectException"))) {
				// JDK 16 workaround - ObjectMapper seems not be JDK16 compatible
				// with the setup present in Spring Cloud Contract. So we will not
				// allow patching but we will just copy values from the patch to
				// to the object to merge
				try {
					YamlPropertiesFactoryBean yamlProcessor = new YamlPropertiesFactoryBean();
					yamlProcessor.setResources(new ByteArrayResource(bytes));
					Properties properties = yamlProcessor.getObject();
					T props = (T) new Binder(
							new MapConfigurationPropertySource(properties.entrySet()
								.stream()
								.collect(Collectors.toMap(entry -> entry.getKey().toString(),
										entry -> entry.getValue().toString()))))
						.bind("", objectToMerge.getClass())
						.get();
					BeanUtils.copyProperties(props, objectToMerge);
					return objectToMerge;
				}
				catch (Exception ex) {
					throw new IllegalStateException(ex);
				}
			}
			throw new IllegalStateException(e);
		}
	}

	public static MetadataMap map() {
		return new MetadataMap();
	}

	private static JsonMapper buildJsonMapper() {
		return JsonMapper.builder()
			.withConfigOverride(Object.class,
					o -> o.setIncludeAsProperty(
							JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL)))
			.withConfigOverride(Object.class,
					o -> o.setIncludeAsProperty(JsonInclude.Value.construct(JsonInclude.Include.NON_DEFAULT,
							JsonInclude.Include.NON_DEFAULT)))
			.withConfigOverride(Object.class,
					o -> o.setIncludeAsProperty(
							JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, JsonInclude.Include.NON_EMPTY)))
			.withConfigOverride(Object.class,
					o -> o.setIncludeAsProperty(JsonInclude.Value.construct(JsonInclude.Include.NON_ABSENT,
							JsonInclude.Include.NON_ABSENT)))
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			.addMixIn(Object.class, PropertyFilterMixIn.class)
			.filterProvider(new SimpleFilterProvider().addFilter("non default properties", new MyFilter()))
			.build();
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

@JsonFilter("non default properties")
class PropertyFilterMixIn {

}

class MyFilter extends SimpleBeanPropertyFilter implements Serializable {

	private static final Map<Class, Object> CACHE = new ConcurrentHashMap<>();

	@Override
	public void serializeAsProperty(Object pojo, JsonGenerator jgen, SerializationContext provider,
			PropertyWriter writer) throws Exception {
		if (pojo instanceof Map || pojo instanceof Collection) {
			writer.serializeAsProperty(pojo, jgen, provider);
			return;
		}
		Object defaultInstance = defaultInstance(pojo);
		if (defaultInstance instanceof CantInstantiateThisClass
				|| !valueSameAsDefault(pojo, defaultInstance, writer.getName())) {
			writer.serializeAsProperty(pojo, jgen, provider);
		}
	}

	Object defaultInstance(Object pojo) {
		return CACHE.computeIfAbsent(pojo.getClass(), this::defaultInstance);
	}

	private Object defaultInstance(Class aClass) {
		try {
			return aClass.getDeclaredConstructor().newInstance();
		}
		catch (Exception e) {
			return new CantInstantiateThisClass();
		}
	}

	boolean valueSameAsDefault(Object pojo, Object defaultInstance, String fieldName) {
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
