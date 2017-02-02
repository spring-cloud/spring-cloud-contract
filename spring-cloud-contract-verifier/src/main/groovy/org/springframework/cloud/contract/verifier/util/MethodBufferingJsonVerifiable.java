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

package org.springframework.cloud.contract.verifier.util;

import com.toomuchcoding.jsonassert.JsonVerifiable;

/**
 * A wrapper over {@link JsonVerifiable} that allows to store the method
 * name in order to print it out in the generated test
 *
 * @author Marcin Grzejszczak
 *
 * @since 1.0.0
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
	MethodBufferingJsonVerifiable isEmpty();

	@Override
	MethodBufferingJsonVerifiable matches(String value);

	@Override
	MethodBufferingJsonVerifiable isEqualTo(Boolean value);

	@Override
	MethodBufferingJsonVerifiable value();

	boolean assertsSize();

	boolean assertsConcreteValue();
}
