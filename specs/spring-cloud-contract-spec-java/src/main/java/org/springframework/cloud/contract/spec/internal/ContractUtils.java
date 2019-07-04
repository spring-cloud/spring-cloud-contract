/*
 * Copyright 2013-2019 the original author or authors.
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

package org.springframework.cloud.contract.spec.internal;

import java.util.function.Function;

final class ContractUtils {

	static final Function CLIENT_VALUE = o -> o instanceof DslProperty
			? ((DslProperty) o).getClientValue() : o;
	static final Function SERVER_VALUE = o -> o instanceof DslProperty
			? ((DslProperty) o).getServerValue() : o;

	private ContractUtils() {
		throw new IllegalStateException("Can't instantiate an utility class");
	}

	static Object convertStubSideRecursively(Object object) {
		return convertRecursively(object, CLIENT_VALUE);
	}

	static Object convertTestSideRecursively(Object object) {
		return convertRecursively(object, SERVER_VALUE);
	}

	private static Object convertRecursively(Object object, Function function) {
		if (object instanceof DslProperty) {
			return convertRecursively(function.apply(object), function);
		}
		return object;
	}

}
