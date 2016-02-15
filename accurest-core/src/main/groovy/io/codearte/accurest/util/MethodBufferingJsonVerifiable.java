package io.codearte.accurest.util;

import com.blogspot.toomuchcoding.jsonassert.JsonVerifiable;

/**
 * @author Marcin Grzejszczak
 */
public interface MethodBufferingJsonVerifiable
		extends JsonVerifiable, MethodBuffering {
	@Override
	MethodBufferingJsonVerifiable contains(Object value);

	@Override
	MethodBufferingJsonVerifiable field(Object value);

	@Override
	MethodBufferingJsonVerifiable array(Object value);

	MethodBufferingJsonVerifiable arrayField(Object value);

	@Override
	MethodBufferingJsonVerifiable arrayField();

	@Override
	MethodBufferingJsonVerifiable array();

	MethodBufferingJsonVerifiable iterationPassingArray();

	@Override
	MethodBufferingJsonVerifiable isEqualTo(String value);

	@Override
	MethodBufferingJsonVerifiable isEqualTo(Object value);

	@Override
	MethodBufferingJsonVerifiable isEqualTo(Number value);

	@Override
	MethodBufferingJsonVerifiable isNull();

	@Override
	MethodBufferingJsonVerifiable matches(String value);

	@Override
	MethodBufferingJsonVerifiable isEqualTo(Boolean value);

	@Override
	MethodBufferingJsonVerifiable value();
}
