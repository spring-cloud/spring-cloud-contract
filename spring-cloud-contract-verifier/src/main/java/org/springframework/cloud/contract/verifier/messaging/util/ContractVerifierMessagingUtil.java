/*
 *  Copyright 2013-2016 the original author or authors.
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

package org.springframework.cloud.contract.verifier.messaging.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Helper class that allows to build headers in a nice way
 *
 * @author Marcin Grzejszczak
 */
public class ContractVerifierMessagingUtil {

	public static ContractVerifierHeaders headers() {
		return new ContractVerifierHeaders();
	}

	public static class ContractVerifierHeaders implements Map<String, Object> {

		private final Map<String, Object> delegate = new HashMap<>();

		public ContractVerifierHeaders header(String key, Object value) {
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
