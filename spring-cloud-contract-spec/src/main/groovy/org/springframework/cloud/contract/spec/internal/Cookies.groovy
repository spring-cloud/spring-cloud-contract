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

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TypeChecked

/**
 * Represents a set of http cookies
 *
 * @author Alex Xandra Albert Sim
 * @since 1.2.5
 */
@EqualsAndHashCode(includeFields = true)
@ToString(includePackage = false, includeFields = true, ignoreNulls = true, includeNames = true)
@TypeChecked
class Cookies {

    Set<Cookie> cookies = []

    void cookie(Map<String, Object> singleCookie) {
        Map.Entry<String, Object> first = singleCookie.entrySet().first()
        cookies << new Cookie(first?.key, first?.value)
    }

    void cookie(String cookieKey, Object cookieValue) {
        cookies << new Cookie(cookieKey, cookieValue)
    }

    void executeForEachCookie(Closure closure) {
        cookies?.each {
            cookie -> closure(cookie)
        }
    }

    DslProperty matching(String value) {
        return new DslProperty(value)
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false
        Cookies cookies = (Cookies) o
        if (cookies != cookies.cookies) return false
        return true
    }

    int hashCode() {
        return cookies.hashCode()
    }
}
