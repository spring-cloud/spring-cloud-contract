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

import java.util.Collections;
import java.util.List;

/**
 * Interface for metadata objects parsed from the metadata in a contract. Will be used to
 * scan for implementations of the interface in order to build a schema of how metadata
 * can look like.
 *
 * @author Marcin Grzejszczak
 * @since 3.0.0
 */
public interface SpringCloudContractMetadata {

	/**
	 * Name of the key under which this metadata entry will be present in contract's
	 * metadata.
	 * @return key name
	 */
	String key();

	/**
	 * Short description of the metadata. Will be used in the generated documentation.
	 * @return description of the metadata.
	 */
	String description();

	/**
	 * Collection of additional classes to look at if one is interested.
	 * @return additional classes
	 */
	default List<Class> additionalClassesToLookAt() {
		return Collections.emptyList();
	}

}
