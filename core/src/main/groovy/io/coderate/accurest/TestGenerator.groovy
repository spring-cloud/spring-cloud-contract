package io.coderate.accurest

import io.coderate.accurest.builder.ClassBuilder
import io.coderate.accurest.config.TestFramework
import io.coderate.accurest.config.TestMode
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
	private final TestMode testMode

	TestGenerator(String stubsBaseDirectory, String basePackageForTests, String baseClassForTests,
	              String ruleClassForTests, TestFramework testFramework, TestMode testMode, String targetDirectory) {
		this.testMode = testMode
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
		this.lang = testFramework
	}

	public String generate() {
		StringBuilder builder = new StringBuilder()
		List<File> files = new File(stubsBaseDirectory).listFiles()
		files.grep({ File file -> file.isDirectory() && containsStubs(file) }).each {
			builder << addClass(it)
			def testBaseDir = Paths.get(targetDirectory, NamesUtil.packageToDirectory(basePackageForTests))
			Files.createDirectories(testBaseDir)
			Files.write(Paths.get(testBaseDir.toString(), NamesUtil.capitalize(it.name) + getTestClassExtension()), addClass(it).bytes)
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

		if (testMode == TestMode.MOCKMVC) {
			clazz.addStaticImport('com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.*')
		} else {
			clazz.addStaticImport('com.jayway.restassured.RestAssured.*')
		}

		if (lang == TestFramework.JUNIT) {
			clazz.addImport('org.junit.Test')
		} else {
			clazz.addImport('groovy.json.JsonSlurper')
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
		print new TestGenerator('/home/devel/projects/codearte/accurest/core/src/main/resources/stubs', 'io.test', '', '', TestFramework.SPOCK, TestMode.MOCKMVC, "").generate()
	}
}