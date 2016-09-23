package org.springframework.cloud.contract.verifier.builder

import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 */
class ClassBuilderSpec extends Specification {

	def "should return explicit base class if provided and no default package for base classes is provided"() {
		given:
			ContractVerifierConfigProperties props = new ContractVerifierConfigProperties(baseClassForTests: 'a.b.Class')
		expect:
			'a.b.Class' == ClassBuilder.retrieveBaseClass(props, 'com/example/foo')
	}

	def "should return a class from the generated path by taking two last folders when package with base classes is provided"() {
		given:
			ContractVerifierConfigProperties props = new ContractVerifierConfigProperties(packageWithBaseClasses: 'com.example.base')
			String contractRelativeFolder = 'com/example/some/superpackage'
		expect:
			ClassBuilder.retrieveBaseClass(props, contractRelativeFolder) == 'com.example.base.SomeSuperpackageBase'
	}

	def "should return a class from the generated path by taking a single folder when package with base classes is provided and there are not enough package elements"() {
		given:
			ContractVerifierConfigProperties props = new ContractVerifierConfigProperties(packageWithBaseClasses: 'com.example.base')
			String contractRelativeFolder = 'superpackage'
		expect:
			ClassBuilder.retrieveBaseClass(props, contractRelativeFolder) == 'com.example.base.SuperpackageBase'
	}

	def "should return a class from mappings regardless of other entries if mapping exists"() {
		given:
			ContractVerifierConfigProperties props = new ContractVerifierConfigProperties(
					packageWithBaseClasses: 'com.example.base',
					baseClassMappings: ['.*' : 'com.example.base.SuperClass'])
			String contractRelativeFolder = 'superpackage'
		expect:
			ClassBuilder.retrieveBaseClass(props, contractRelativeFolder) == 'com.example.base.SuperClass'
	}

	def "should return the first matching base class when provided mapping doesn't match"() {
		given:
			ContractVerifierConfigProperties props = new ContractVerifierConfigProperties(
					baseClassForTests: 'a.b.Class',
					packageWithBaseClasses: 'com.example.base',
					baseClassMappings: ['patternNotMatchingAnything' : 'com.example.base.SuperClass'])
			String contractRelativeFolder = 'superpackage'
		expect:
			ClassBuilder.retrieveBaseClass(props, contractRelativeFolder) == 'com.example.base.SuperpackageBase'
	}

}
