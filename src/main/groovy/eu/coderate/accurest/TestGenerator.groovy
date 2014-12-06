package eu.coderate.accurest

import eu.coderate.accurest.util.NamesUtil

import static eu.coderate.accurest.builder.ClassBuilder.createClass
import static eu.coderate.accurest.builder.MethodBuilder.createVoidMethod

/**
 * @author Jakub Kubrynski
 */
class TestGenerator {

	private final String mocksBaseDirectory
	private final String basePackageForTests
	private final String baseClassForTests
	private final String ruleClassForTests

	TestGenerator(String mocksBaseDirectory, String basePackageForTests, String baseClassForTests, String ruleClassForTests) {
		this.mocksBaseDirectory = getClass().getClassLoader().getResource(mocksBaseDirectory).path
		this.basePackageForTests = basePackageForTests
		this.baseClassForTests = baseClassForTests
		this.ruleClassForTests = ruleClassForTests
	}

	public String generate() {
		StringBuilder builder = new StringBuilder()
		List<String> files = new File(mocksBaseDirectory).list()
		files.each {
			builder << addClass(NamesUtil.capitalize(NamesUtil.toLastDot(it)))
		}
		return builder
	}

	private String addClass(String className) {
		createClass(className, basePackageForTests, baseClassForTests)
				.addImport('org.junit.Test')
				.addImport('org.junit.Rule')
				.addStaticImport('org.hamcrest.Matchers.*')
				.addStaticImport('com.jayway.restassured.RestAssured.*')
				.addStaticImport('com.jayway.restassured.matcher.RestAssuredMatchers.*')
				.addRule('com.test.MyRule')
				.addMethod(createVoidMethod('shouldInvokeService'))
				.build()
	}

	public static void main(String[] args) {
		print new TestGenerator("wiremocks", 'io.test', 'com.test.BaseClass', "").generate()
	}
}