package eu.coderate.accurest

import eu.coderate.accurest.builder.ClassBuilder
import eu.coderate.accurest.util.NamesUtil

import static eu.coderate.accurest.builder.ClassBuilder.createClass
import static eu.coderate.accurest.builder.MethodBuilder.createTestMethod

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
		List<File> files = new File(mocksBaseDirectory).listFiles()
		files.grep({ File file -> file.isDirectory() && containsStubs(file) }).each {
			builder << addClass(it)
		}
		return builder
	}

	boolean containsStubs(File file) {
		return file.list(new FilenameFilter() {
			@Override
			boolean accept(File dir, String name) {
				return "json".equalsIgnoreCase(NamesUtil.afterLastDot(name))
			}
		}).size() > 0
	}

	private String addClass(File directory) {
		ClassBuilder clazz = createClass(NamesUtil.capitalize(NamesUtil.afterLast(directory.path, '/')), basePackageForTests, baseClassForTests)
				.addImport('org.junit.Test')
				.addImport('org.junit.Rule')
				.addStaticImport('org.hamcrest.Matchers.*')
				.addStaticImport('com.jayway.restassured.RestAssured.*')
				.addStaticImport('com.jayway.restassured.matcher.RestAssuredMatchers.*')
				.addRule('com.test.MyRule')
		directory.listFiles().each {
			clazz.addMethod(createTestMethod(it))
		}
		return clazz.build()
	}

	public static void main(String[] args) {
		print new TestGenerator("", 'io.test', 'com.test.BaseClass', "").generate()
	}
}