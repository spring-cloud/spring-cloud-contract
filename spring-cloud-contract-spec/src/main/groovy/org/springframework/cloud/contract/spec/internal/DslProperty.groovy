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

package org.springframework.cloud.contract.spec.internal

import groovy.transform.CompileStatic
import groovy.transform.ToString
/**
 * Represents an element of a DSL that can contain client or sever side values
 *
 * @since 1.0.0
 */
@CompileStatic
@ToString(includePackage = false, includeNames = true)
class DslProperty<T> implements Serializable {

	final T clientValue
	final T serverValue

	DslProperty(T clientValue, T serverValue) {
		this.clientValue = clientValue
		this.serverValue = serverValue
	}

	DslProperty(T singleValue) {
		this.clientValue = singleValue
		this.serverValue = singleValue
	}

	boolean isSingleValue() {
		return this.clientValue == this.serverValue ||
				(this.clientValue != null && this.serverValue == null ) ||
				(this.serverValue != null && this.clientValue == null )
	}

	boolean equals(o) {
		if (this.is(o)) return true
		if (getClass() != o.class) return false
		DslProperty that = (DslProperty) o
		if (this.clientValue != that.clientValue) return false
		if (this.serverValue != that.serverValue) return false
		return true
	}

	int hashCode() {
		int result
		result = (this.clientValue != null ? this.clientValue.hashCode() : 0)
		result = 31 * result + (this.serverValue != null ? this.serverValue.hashCode() : 0)
		return result
	}
}
