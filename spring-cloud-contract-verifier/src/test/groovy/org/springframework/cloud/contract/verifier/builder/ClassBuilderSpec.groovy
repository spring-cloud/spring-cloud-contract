package org.springframework.cloud.contract.verifier.builder

import spock.lang.Issue
import spock.lang.Specification

import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties

/**
 * @author Marcin Grzejszczak
 */
class ClassBuilderSpec extends Specification {

	def "should return explicit base class if provided and no default package for base classes is provided"() {
		given:
			ContractVerifierConfigProperties props = new ContractVerifierConfigProperties(baseClassForTests: 'a.b.Class')
		expect:
			'a.b.Class' == new BaseClassProvider().retrieveBaseClass(props, 'com/example/foo')
	}

	def "should return a class from the generated path by taking two last folders when package with base classes is provided"() {
		given:
			ContractVerifierConfigProperties props = new ContractVerifierConfigProperties(packageWithBaseClasses: 'com.example.base')
			String contractRelativeFolder = ['com', 'example', 'some', 'superpackage'].join(File.separator)
		expect:
			new BaseClassProvider().retrieveBaseClass(props, contractRelativeFolder) == 'com.example.base.SomeSuperpackageBase'
	}

	def "should return a class from the generated path by taking two last folders when package with base classes is provided and contains invalid chars"() {
		given:
			ContractVerifierConfigProperties props = new ContractVerifierConfigProperties(packageWithBaseClasses: 'com.example.base')
			String contractRelativeFolder = ['com', 'example', 'beer-api-producer-external', 'beer-api-consumer'].join(File.separator)
		expect:
			new BaseClassProvider().retrieveBaseClass(props, contractRelativeFolder) == 'com.example.base.Beer_api_producer_externalBeer_api_consumerBase'
	}

	def "should return a class from the generated path by taking a single folder when package with base classes is provided and there are not enough package elements"() {
		given:
			ContractVerifierConfigProperties props = new ContractVerifierConfigProperties(packageWithBaseClasses: 'com.example.base')
			String contractRelativeFolder = 'superpackage'
		expect:
			new BaseClassProvider().retrieveBaseClass(props, contractRelativeFolder) == 'com.example.base.SuperpackageBase'
	}

	def "should return a class from mappings regardless of other entries if mapping exists"() {
		given:
			ContractVerifierConfigProperties props = new ContractVerifierConfigProperties(
					packageWithBaseClasses: 'com.example.base',
					baseClassMappings: ['.*': 'com.example.base.SuperClass'])
			String contractRelativeFolder = 'superpackage'
		expect:
			new BaseClassProvider().retrieveBaseClass(props, contractRelativeFolder) == 'com.example.base.SuperClass'
	}

	@Issue("701")
	def "should match base class when mapping regex has multiple folders"() {
		given:
			ContractVerifierConfigProperties props = new ContractVerifierConfigProperties(
					baseClassMappings: ['.*bar.baz.some.*': 'com.example.base.SuperClass'])
			String contractRelativeFolder = 'foo/bar/baz/some/package'.split("/").join(File.separator)
		expect:
			new BaseClassProvider().retrieveBaseClass(props, contractRelativeFolder) == 'com.example.base.SuperClass'
	}

	def "should return the first matching base class when provided mapping doesn't match"() {
		given:
			ContractVerifierConfigProperties props = new ContractVerifierConfigProperties(
					baseClassForTests: 'a.b.Class',
					packageWithBaseClasses: 'com.example.base',
					baseClassMappings: ['patternNotMatchingAnything': 'com.example.base.SuperClass'])
			String contractRelativeFolder = 'superpackage'
		expect:
			new BaseClassProvider().retrieveBaseClass(props, contractRelativeFolder) == 'com.example.base.SuperpackageBase'
	}

	def "should return a class from the generated path by when external contracts are picked"() {
		given:
			ContractVerifierConfigProperties props = new ContractVerifierConfigProperties(packageWithBaseClasses: "foo.Bar")
			String contractRelativeFolder = ["org", "springframework", "cloud", "contract", "verifier", "tests", "META_INF", "com.example", "hello_world", "0.1.0_dev.1.uncommitted+d1174dd"].join(File.separator)
		expect:
			new BaseClassProvider().retrieveBaseClass(props, contractRelativeFolder) == 'foo.Bar.Hello_world0_1_0_dev_1_uncommitted_d1174ddBase'
	}

}
