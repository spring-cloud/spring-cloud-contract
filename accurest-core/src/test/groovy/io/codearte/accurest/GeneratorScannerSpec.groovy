package io.codearte.accurest

import io.codearte.accurest.config.AccurestConfigProperties
import spock.lang.Specification

class GeneratorScannerSpec extends Specification {

	private SingleTestGenerator classGenerator = Mock(SingleTestGenerator)

	def "should find all .json files and generate 6 classes for them"() {
		given:
			File resource = new File(this.getClass().getResource("/directory/with/stubs/stubsRepositoryIndicator").toURI())
			AccurestConfigProperties properties = new AccurestConfigProperties()
			properties.contractsDslDir = resource.parentFile
			TestGenerator testGenerator = new TestGenerator(properties, classGenerator, Stub(FileSaver))
		when:
			testGenerator.generateTestClasses("com.ofg")
		then:
			6 * classGenerator.buildClass(_, _, _) >> "qwerty"
	}

	def "should create class with full package"() {
		given:
			AccurestConfigProperties properties = new AccurestConfigProperties()
			properties.contractsDslDir = new File(this.getClass().getResource("/directory/with/stubs/package").toURI())
			TestGenerator testGenerator = new TestGenerator(properties, classGenerator, Stub(FileSaver))
		when:
			testGenerator.generateTestClasses("com.ofg")
		then:
			1 * classGenerator.buildClass(_, 'exceptionsSpec', 'com.ofg') >> "spec"
			1 * classGenerator.buildClass(_, 'exceptionsSpec', 'com.ofg.v1') >> "spec1"
			1 * classGenerator.buildClass(_, 'exceptionsSpec', 'com.ofg.v2') >> "spec2"
	}

}
