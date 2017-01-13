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

package org.springframework.cloud.contract.spec.internal

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * Represents a property that has name and content. Used together with
 * multipart requests.
 *
 * @since 1.0.0
 */
@ToString(includePackage = false, includeNames = true)
@EqualsAndHashCode(includeFields = true)
@CompileStatic
class NamedProperty {

    private static final String NAME = 'name'
    private static final String CONTENT = 'content'

    DslProperty name
    DslProperty value

    NamedProperty(DslProperty name, DslProperty value) {
        this.name = name
        this.value = value
    }
    
    NamedProperty(Map<String, DslProperty> namedMap) {
        this(namedMap?.get(NAME), namedMap?.get(CONTENT))
    }
}
