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

/**
 * Exception occurring when we're trying to parse the DSL.
 *
 * @author Marcin Grzejszczak
 * @since 1.1.0
 */
public class DslParseException extends RuntimeException {

	public DslParseException(String message) {
		super(message);
	}

	public DslParseException(Throwable cause) {
		super(cause);
	}

}
