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

package org.springframework.cloud.contract.spec.internal

/**
 * Represents the cookies when sending/receiving HTTP traffic.
 *
 * @author Tim Ysewyn
 * @since 2.2.0
 */
open class CookiesDsl : CommonDsl() {

    private val cookies = LinkedHashMap<String, Any>()

    open fun matching(value: Any?): Any? = value

    /**
     * Adds a cookie.
     *
     * @param name The name of the cookie.
     * @param value The value of the cookie.
     */
    fun cookie(name: String, value: Any) {
        this.cookies[name] = value
    }

    internal fun get(): Cookies {
        val cookies = Cookies()
        this.cookies.forEach { (name, value) -> cookies.cookie(name, value) }
        return cookies
    }

}
