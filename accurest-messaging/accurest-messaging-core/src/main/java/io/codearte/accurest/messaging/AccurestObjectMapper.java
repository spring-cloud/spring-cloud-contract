package io.codearte.accurest.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Wrapper over {@link ObjectMapper} that won't try to parse
 * String but will directly return it.
 *
 * @author Marcin Grzejszczak
 */
public class AccurestObjectMapper {

	private final ObjectMapper objectMapper;

	public AccurestObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public AccurestObjectMapper() {
		this.objectMapper = new ObjectMapper();
	}

	public String writeValueAsString(Object payload) throws JsonProcessingException {
		if (payload instanceof String) {
			return payload.toString();
		}
		return objectMapper.writeValueAsString(payload);
	}
}
