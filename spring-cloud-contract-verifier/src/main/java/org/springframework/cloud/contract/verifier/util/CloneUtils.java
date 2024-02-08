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

import org.springframework.util.SerializationUtils;

/**
 * Creates a clone.
 *
 * @author Marcin Grzejszczak
 * @since 2.2.0
 */
public final class CloneUtils {

	private CloneUtils() {
		throw new IllegalStateException("Can't instantiate an utility class");
	}

	/**
	 * Clones an object if it's serializable.
	 * @param object to clone
	 * @return a clone of the object
	 */
	public static Object clone(Object object) {
		byte[] serializedObject = SerializationUtils.serialize(object);
		return SerializationUtils.deserialize(serializedObject);
	}

}
