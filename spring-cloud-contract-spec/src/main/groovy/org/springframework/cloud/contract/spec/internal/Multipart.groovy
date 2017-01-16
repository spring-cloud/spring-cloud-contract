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
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includePackage = false, includeFields = true, includeNames = true, includeSuper = true)
@EqualsAndHashCode(callSuper = true)
@CompileStatic
class Multipart extends DslProperty {

    Multipart(Map<String, DslProperty> multipart) {
        super(extractValue(multipart, { DslProperty p -> p.clientValue}), extractValue(multipart, {DslProperty p -> p.serverValue}))
    }

    private static Map<String, Object> extractValue(Map<String, DslProperty> multipart, Closure valueProvider) {
        return multipart.collectEntries { Map.Entry<String, DslProperty> entry ->
            [(entry.key): valueProvider(entry.value)]
        } as Map<String, Object>
    }

    Multipart(List<DslProperty> multipartAsList) {
        super(multipartAsList.collect { DslProperty p -> p.clientValue }, multipartAsList.collect { DslProperty p -> p.serverValue })
    }

    Multipart(Object multipartAsValue) {
        this("${multipartAsValue}")
    }

    Multipart(GString multipartAsValue) {
        super(multipartAsValue, multipartAsValue)
    }

    Multipart(DslProperty multipartAsValue) {
        super(multipartAsValue.clientValue, multipartAsValue.serverValue)
    }

    Multipart(MatchingStrategy matchingStrategy) {
        super(matchingStrategy, matchingStrategy)
    }
}
