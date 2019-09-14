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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Represents a set of http cookies.
 *
 * @author Alex Xandra Albert Sim
 * @since 1.2.5
 */
public class Cookies {

	private Set<Cookie> entries = new HashSet<>();

	public void cookie(Map<String, Object> singleCookie) {
		Iterator<Map.Entry<String, Object>> iterator = singleCookie.entrySet().iterator();
		if (iterator.hasNext()) {
			Map.Entry<String, Object> first = iterator.next();
			if (first != null) {
				entries.add(Cookie.build(first.getKey(), first.getValue()));
			}
		}
	}

	public void cookie(String cookieKey, Object cookieValue) {
		entries.add(Cookie.build(cookieKey, cookieValue));
	}

	public void executeForEachCookie(final Consumer<Cookie> consumer) {
		entries.forEach(consumer::accept);
	}

	public DslProperty matching(String value) {
		return new DslProperty(value);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Cookies cookies = (Cookies) o;
		return Objects.equals(entries, cookies.entries);
	}

	@Override
	public int hashCode() {
		return Objects.hash(entries);
	}

	@Override
	public String toString() {
		return "Cookies{" + "entries=" + entries + '}';
	}

	/**
	 * Converts the headers into their stub side representations and returns as a map of
	 * String key =&gt; Object value.
	 * @return converted map
	 */
	public Map<String, Object> asStubSideMap() {
		final Map<String, Object> map = new LinkedHashMap<>();
		entries.forEach(cookie -> map.put(cookie.getKey(),
				ContractUtils.convertStubSideRecursively(cookie)));
		return map;
	}

	/**
	 * Converts the headers into their stub side representations and returns as a map of
	 * String key =&gt; Object value.
	 * @return converted map
	 */
	public Map<String, Object> asTestSideMap() {
		final Map<String, Object> map = new HashMap<String, Object>();
		entries.forEach(cookie -> map.put(cookie.getKey(),
				ContractUtils.convertTestSideRecursively(cookie)));
		return map;
	}

	public Set<Cookie> getEntries() {
		return entries;
	}

	public void setEntries(Set<Cookie> entries) {
		this.entries = entries;
	}

}
