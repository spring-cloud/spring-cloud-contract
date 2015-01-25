package io.coderate.accurest

import io.coderate.accurest.builder.ClassBuilder
import io.coderate.accurest.builder.TestFramework
import io.coderate.accurest.util.NamesUtil

import java.nio.file.Files
import java.nio.file.Paths

import static ClassBuilder.createClass
import static io.coderate.accurest.builder.MethodBuilder.createTestMethod

/**
 * @author Jakub Kubrynski
 */
class TestGenerator {

	private final String stubsBaseDirectory
	private final String basePackageForTests
	private final String baseClassForTests
	private final String ruleClassForTests
	private final TestFramework lang
	private final String targetDirectory

	TestGenerator(String stubsBaseDirectory, String basePackageForTests, String baseClassForTests,
	              String ruleClassForTests, String testFramework, String targetDirectory) {
//		URL stubsResource = getClass().getClassLoader().getResource(stubsBaseDirectory)
		this.targetDirectory = targetDirectory
		File stubsResource = new File(stubsBaseDirectory)
		if (stubsResource == null) {
			throw new IllegalStateException("Stubs directory not found under " + stubsBaseDirectory)
		}
		this.stubsBaseDirectory = stubsResource.path
		this.basePackageForTests = basePackageForTests
		if (testFramework == 'Spock' && !baseClassForTests) {
			this.baseClassForTests = 'spock.lang.Specification'
		} else {
			this.baseClassForTests = baseClassForTests
		}
		this.ruleClassForTests = ruleClassForTests
		this.lang = testFramework == 'Spock' ? TestFramework.SPOCK : TestFramework.JUNIT
	}

	public String generate() {
		StringBuilder builder = new StringBuilder()
		List<File> files = new File(stubsBaseDirectory).listFiles()
		files.grep({ File file -> file.isDirectory() && containsStubs(file) }).each {
			builder << addClass(it)
			def testBaseDir = Paths.get(targetDirectory, NamesUtil.packageToDirectory(basePackageForTests))
			Files.createDirectories(testBaseDir)
			Files.write(Paths.get(testBaseDir.toString(), NamesUtil.capitalize(it.name) + getTestClassExtension()), addClass(it).bytes)
			println testBaseDir
		}
		return builder
	}

	private String getTestClassExtension() {
		return lang == TestFramework.SPOCK ? '.groovy' : '.java'
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

		if (lang == TestFramework.JUNIT) {
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
		print new TestGenerator('stubs', 'io.test', '', '', 'Spock').generate()
	}
}