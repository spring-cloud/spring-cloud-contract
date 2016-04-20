package io.codearte.accurest.messaging;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Helper class that allows to build headers in a nice way
 *
 * @author Marcin Grzejszczak
 */
public class AccurestMessagingUtil {

	public static AccurestHeaders headers() {
		return new AccurestHeaders();
	}

	public static class AccurestHeaders implements Map<String, Object> {

		private final Map<String, Object> delegate = new HashMap<>();

		public AccurestHeaders header(String key, Object value) {
			put(key, value);
			return this;
		}

		@Override
		public int size() {
			return delegate.size();
		}

		@Override
		public boolean isEmpty() {
			return delegate.isEmpty();
		}

		@Override
		public boolean containsKey(Object key) {
			return delegate.containsKey(key);
		}

		@Override
		public boolean containsValue(Object value) {
			return delegate.containsValue(value);
		}

		@Override
		public Object get(Object key) {
			return delegate.get(key);
		}

		@Override
		public Object put(String key, Object value) {
			return delegate.put(key, value);
		}

		@Override
		public Object remove(Object key) {
			return delegate.remove(key);
		}

		@Override
		public void putAll(Map<? extends String, ?> m) {
			delegate.putAll(m);
		}

		@Override
		public void clear() {
			delegate.clear();
		}

		@Override
		public Set<String> keySet() {
			return delegate.keySet();
		}

		@Override
		public Collection<Object> values() {
			return delegate.values();
		}

		@Override
		public Set<Entry<String, Object>> entrySet() {
			return delegate.entrySet();
		}

		@Override
		public boolean equals(Object o) {
			return delegate.equals(o);
		}

		@Override
		public int hashCode() {
			return delegate.hashCode();
		}
	}
}
