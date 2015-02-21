package io.codearte.accurest

import io.coderate.accurest.FileSaver
import io.coderate.accurest.SingleTestGenerator
import io.coderate.accurest.TestGenerator
import io.coderate.accurest.config.AccurestConfigProperties
import spock.lang.Specification

class TestGeneratorSpec extends Specification {

	private SingleTestGenerator classGenerator = Mock(SingleTestGenerator)

	def "should find all .json files and generate 3 classes for them"() {
		given:
			File resource = new File(this.getClass().getResource("/directory/with/stubs/stubsRepositoryIndicator").toURI())
			AccurestConfigProperties properties = new AccurestConfigProperties()
			properties.stubsBaseDirectory = resource.parentFile
			TestGenerator testGenerator = new TestGenerator(properties, classGenerator, Stub(FileSaver))
		when:
			testGenerator.generateTestClasses("com.ofg")
		then:
			3 * classGenerator.buildClass(_, _, _) >> "qwerty"
	}

	def "should filter other directory"() {
		given:
			File resource = new File(this.getClass().getResource("/directory/with/stubs/stubsRepositoryIndicator").toURI())
			AccurestConfigProperties properties = new AccurestConfigProperties()
			properties.ignoredFiles << "**/other/**"
			properties.stubsBaseDirectory = resource.parentFile
			TestGenerator testGenerator = new TestGenerator(properties, classGenerator, Stub(FileSaver))
		when:
			testGenerator.generateTestClasses("com.ofg")
		then:
			1 * classGenerator.buildClass(_, 'different', _) >> "qwerty"
	}

	def "should ignore file"() {
		given:
			File resource = new File(this.getClass().getResource("/directory/with/stubs/stubsRepositoryIndicator").toURI())
			AccurestConfigProperties properties = new AccurestConfigProperties()
			properties.ignoredFiles << "**/other.json"
			properties.stubsBaseDirectory = resource.parentFile
			TestGenerator testGenerator = new TestGenerator(properties, classGenerator, Stub(FileSaver))
			classGenerator.buildClass(_, _, _) >> "sample"
		when:
			testGenerator.generateTestClasses("com.ofg")
		then:
			1 * classGenerator.buildClass({ it.size() == 1 }, 'other', _) >> "sample.json"
	}
}
