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

package org.springframework.cloud.contract.spec.internal

/**
 * @author Tim Ysewyn
 */
open class CookiesDsl : CommonDsl() {
    
    private val cookies = LinkedHashMap<String, Any>()

    open fun matching(value: Any?): Any? = value

    /**
     * Adds and configures a cookie.
     *
     * @param configurer The lambda to configure the cookie.
     */
    fun cookie(configurer: CookieDsl.() -> Unit) {
        try {
            val cookie = CookieDsl().apply(configurer)
            this.cookies[cookie.name] = cookie.value
        } catch (ex: IllegalStateException) {
            throw IllegalStateException("Cookie is missing its name or value")
        }
    }

    internal fun get(): Cookies {
        val cookies = Cookies()
        this.cookies.forEach { (name, value) -> cookies.cookie(name, value) }
        return cookies
    }

}
