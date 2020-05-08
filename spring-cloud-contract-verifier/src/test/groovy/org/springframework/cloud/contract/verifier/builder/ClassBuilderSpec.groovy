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

package org.springframework.cloud.contract.verifier.builder


import spock.lang.Issue
import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 */
class ClassBuilderSpec extends Specification {

	def "should return explicit base class if provided and no default package for base classes is provided"() {
		given:
			Map<String, String> baseClassMappings = null
			String packageWithBaseClasses = null
			String baseClassForTests = 'a.b.Class'
		expect:
			'a.b.Class' == new BaseClassProvider().retrieveBaseClass(baseClassMappings, packageWithBaseClasses, baseClassForTests, 'com/example/foo')
	}

	def "should return a class from the generated path by taking two last folders when package with base classes is provided"() {
		given:
			Map<String, String> baseClassMappings = null
			String packageWithBaseClasses = 'com.example.base'
			String baseClassForTests = null
			String contractRelativeFolder = ['com', 'example', 'some', 'superpackage'].join(File.separator)
		expect:
			new BaseClassProvider().retrieveBaseClass(baseClassMappings, packageWithBaseClasses, baseClassForTests, contractRelativeFolder) == 'com.example.base.SomeSuperpackageBase'
	}

	def "should return a class from the generated path by taking two last folders when package with base classes is provided and contains invalid chars"() {
		given:
			Map<String, String> baseClassMappings = null
			String packageWithBaseClasses = 'com.example.base'
			String baseClassForTests = null
			String contractRelativeFolder = ['com', 'example', 'beer-api-producer-external', 'beer-api-consumer'].join(File.separator)
		expect:
			new BaseClassProvider().retrieveBaseClass(baseClassMappings, packageWithBaseClasses, baseClassForTests, contractRelativeFolder) == 'com.example.base.Beer_api_producer_externalBeer_api_consumerBase'
	}

	def "should return a class from the generated path by taking a single folder when package with base classes is provided and there are not enough package elements"() {
		given:
			Map<String, String> baseClassMappings = null
			String packageWithBaseClasses = 'com.example.base'
			String baseClassForTests = null
			String contractRelativeFolder = 'superpackage'
		expect:
			new BaseClassProvider().retrieveBaseClass(baseClassMappings, packageWithBaseClasses, baseClassForTests, contractRelativeFolder) == 'com.example.base.SuperpackageBase'
	}

	def "should return a class from mappings regardless of other entries if mapping exists"() {
		given:
			Map<String, String> baseClassMappings = ['.*': 'com.example.base.SuperClass']
			String packageWithBaseClasses = 'com.example.base'
			String baseClassForTests = null
			String contractRelativeFolder = 'superpackage'
		expect:
			new BaseClassProvider().retrieveBaseClass(baseClassMappings, packageWithBaseClasses, baseClassForTests, contractRelativeFolder) == 'com.example.base.SuperClass'
	}

	@Issue("701")
	def "should match base class when mapping regex has multiple folders"() {
		given:
			Map<String, String> baseClassMappings = ['.*bar.baz.some.*': 'com.example.base.SuperClass']
			String packageWithBaseClasses = null
			String baseClassForTests = null
			String contractRelativeFolder = 'foo/bar/baz/some/package'.split("/").join(File.separator)
		expect:
			new BaseClassProvider().retrieveBaseClass(baseClassMappings, packageWithBaseClasses, baseClassForTests, contractRelativeFolder) == 'com.example.base.SuperClass'
	}

	def "should return the first matching base class when provided mapping doesn't match"() {
		given:
			Map<String, String> baseClassMappings = ['patternNotMatchingAnything': 'com.example.base.SuperClass']
			String packageWithBaseClasses = 'com.example.base'
			String baseClassForTests = 'a.b.Class'
			String contractRelativeFolder = 'superpackage'
		expect:
			new BaseClassProvider().retrieveBaseClass(baseClassMappings, packageWithBaseClasses, baseClassForTests, contractRelativeFolder) == 'com.example.base.SuperpackageBase'
	}

	def "should return a class from the generated path by when external contracts are picked"() {
		given:
			Map<String, String> baseClassMappings = null
			String packageWithBaseClasses = 'foo.Bar'
			String baseClassForTests = null
			String contractRelativeFolder = ["org", "springframework", "cloud", "contract", "verifier", "tests", "META_INF", "com.example", "hello_world", "0.1.0_dev.1.uncommitted+d1174dd"].join(File.separator)
		expect:
			new BaseClassProvider().retrieveBaseClass(baseClassMappings, packageWithBaseClasses, baseClassForTests, contractRelativeFolder) == 'foo.Bar.Hello_world0_1_0_dev_1_uncommitted_d1174ddBase'
	}

}
