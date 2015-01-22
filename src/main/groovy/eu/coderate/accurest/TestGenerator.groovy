package eu.coderate.accurest

import eu.coderate.accurest.builder.ClassBuilder
import eu.coderate.accurest.builder.Lang
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
	private final Lang lang

	TestGenerator(String mocksBaseDirectory, String basePackageForTests, String baseClassForTests, String ruleClassForTests, Lang lang) {
		this.mocksBaseDirectory = getClass().getClassLoader().getResource(mocksBaseDirectory).path
		this.basePackageForTests = basePackageForTests
		if (lang == Lang.GROOVY && !baseClassForTests) {
			this.baseClassForTests = 'spock.lang.Specification'
		} else {
			this.baseClassForTests = baseClassForTests
		}
		this.ruleClassForTests = ruleClassForTests
		this.lang = lang
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
		ClassBuilder clazz = createClass(NamesUtil.capitalize(NamesUtil.afterLast(directory.path, '/')), basePackageForTests, baseClassForTests, lang)
				.addStaticImport('com.jayway.restassured.RestAssured.*')
				.addImport('java.util.regex.Pattern')

		if (lang == Lang.JAVA) {
			clazz.addImport('org.junit.Test')
		}

		if (ruleClassForTests) {
			clazz.addImport('org.junit.Rule')
					.addRule(ruleClassForTests)
		}

		directory.listFiles().each {
			clazz.addMethod(createTestMethod(it, lang))
		}
		return clazz.build()
	}

	public static void main(String[] args) {
		print new TestGenerator('stubs', 'io.test', '', '', Lang.GROOVY).generate()
	}
}