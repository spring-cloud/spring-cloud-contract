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

package org.springframework.cloud.contract.verifier.builder;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Used to return the {@link Class} against which the type of the element should be
 * verified using <code>instanceof</code> in generated response assertions.
 *
 * @author Marcin Grzejszczak
 * @author Olga Maciaszek-Sharma
 * @since 2.1.0
 */
public interface ClassVerifier {

	default Class classToCheck(Object elementFromBody) {
		if (elementFromBody instanceof List) {
			return List.class;
		}
		else if (elementFromBody instanceof Set) {
			return Set.class;
		}
		else if (elementFromBody instanceof Map) {
			return Map.class;
		}
		return elementFromBody.getClass();
	}

}
