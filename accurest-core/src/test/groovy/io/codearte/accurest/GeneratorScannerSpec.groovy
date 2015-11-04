package io.codearte.accurest

import io.codearte.accurest.config.AccurestConfigProperties
import spock.lang.Specification

class GeneratorScannerSpec extends Specification {

	private SingleTestGenerator classGenerator = Mock(SingleTestGenerator)

	def "should find all .json files and generate 3 classes for them"() {
		given:
			File resource = new File(this.getClass().getResource("/directory/with/stubs/stubsRepositoryIndicator").toURI())
			AccurestConfigProperties properties = new AccurestConfigProperties()
			properties.contractsDslDir = resource.parentFile
			TestGenerator testGenerator = new TestGenerator(properties, classGenerator, Stub(FileSaver))
		when:
			testGenerator.generateTestClasses("com.ofg")
		then:
			5 * classGenerator.buildClass(_, _, _) >> "qwerty"
	}

	def "should filter other directory"() {
		given:
			File resource = new File(this.getClass().getResource("/directory/with/stubs/stubsRepositoryIndicator").toURI())
			AccurestConfigProperties properties = new AccurestConfigProperties()
			properties.ignoredFiles << "**/other/**"
			properties.contractsDslDir = resource.parentFile
			TestGenerator testGenerator = new TestGenerator(properties, classGenerator, Stub(FileSaver))
		when:
			testGenerator.generateTestClasses("com.ofg")
		then:
			1 * classGenerator.buildClass(_, 'differentSpec', _) >> "qwerty"
			2 * classGenerator.buildClass(_, 'exceptionsSpec', _) >> "qwerty"
	}

	def "should ignore file"() {
		given:
			File resource = new File(this.getClass().getResource("/directory/with/stubs/stubsRepositoryIndicator").toURI())
			AccurestConfigProperties properties = new AccurestConfigProperties()
			properties.ignoredFiles << "**/other.groovy"
			properties.contractsDslDir = resource.parentFile
			TestGenerator testGenerator = new TestGenerator(properties, classGenerator, Stub(FileSaver))
			classGenerator.buildClass(_, _, _) >> "sample"
		when:
			testGenerator.generateTestClasses("com.ofg")
		then:
			1 * classGenerator.buildClass({ it.size() == 1 }, 'otherSpec', _) >> "sample.groovy"
	}

    def "should create class with full package"() {
        given:
			AccurestConfigProperties properties = new AccurestConfigProperties()
			properties.contractsDslDir = new File(this.getClass().getResource("/directory/with/stubs/package").toURI())
			TestGenerator testGenerator = new TestGenerator(properties, classGenerator, Stub(FileSaver))
        when:
        	testGenerator.generateTestClasses("com.ofg")
        then:
			1 * classGenerator.buildClass(_, 'exceptionsSpec', 'com.ofg.v1') >> "spec1"
			1 * classGenerator.buildClass(_, 'exceptionsSpec', 'com.ofg.v2') >> "spec2"
    }

}
