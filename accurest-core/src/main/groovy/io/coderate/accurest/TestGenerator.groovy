package io.coderate.accurest

import io.coderate.accurest.builder.ClassBuilder
import io.coderate.accurest.config.AccurestConfigProperties
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

	private final String targetDirectory
	private final AccurestConfigProperties configProperties
	private final String stubsBaseDirectory

	TestGenerator(AccurestConfigProperties accurestConfigProperties) {
		this.configProperties = accurestConfigProperties
		this.targetDirectory = accurestConfigProperties.generatedTestSourcesDir
		File stubsResource = new File(accurestConfigProperties.stubsBaseDirectory)
		if (stubsResource == null) {
			throw new IllegalStateException("Stubs directory not found under " + accurestConfigProperties.stubsBaseDirectory)
		}
		this.stubsBaseDirectory = stubsResource.path
	}

	public void generate() {
		List<File> files = new File(stubsBaseDirectory).listFiles()
		files.grep({ File file -> file.isDirectory() && containsStubs(file) }).each {
			def testBaseDir = Paths.get(targetDirectory, NamesUtil.packageToDirectory(configProperties.basePackageForTests))
			Files.createDirectories(testBaseDir)
			Files.write(Paths.get(testBaseDir.toString(), NamesUtil.capitalize(it.name) + getTestClassExtension()), addClass(it).bytes)
		}
	}

	private String getTestClassExtension() {
		return configProperties.targetFramework == TestFramework.SPOCK ? '.groovy' : '.java'
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
		ClassBuilder clazz = createClass(NamesUtil.capitalize(NamesUtil.afterLast(directory.path, '/')), configProperties)

		if (configProperties.imports) {
			configProperties.imports.each {
				clazz.addImport(it)
			}
		}

		if (configProperties.staticImports) {
			configProperties.staticImports.each {
				clazz.addStaticImport(it)
			}
		}

		if (configProperties.testMode == TestMode.MOCKMVC) {
			clazz.addStaticImport('com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.*')
		} else {
			clazz.addStaticImport('com.jayway.restassured.RestAssured.*')
		}

		if (configProperties.targetFramework == TestFramework.JUNIT) {
			clazz.addImport('org.junit.Test')
		} else {
			clazz.addImport('groovy.json.JsonSlurper')
		}

		if (configProperties.ruleClassForTests) {
			clazz.addImport('org.junit.Rule')
					.addRule(configProperties.ruleClassForTests)
		}

		directory.listFiles().each {
			clazz.addMethod(createTestMethod(it, configProperties.targetFramework))
		}
		return clazz.build()
	}

	public static void main(String[] args) {
		AccurestConfigProperties properties = new AccurestConfigProperties(stubsBaseDirectory: '/home/devel/projects/codearte/accurest/accurest-core/src/main/resources/stubs',
		targetFramework: TestFramework.SPOCK, testMode: TestMode.MOCKMVC, basePackageForTests: 'io.test', staticImports: ['com.pupablada.Test.*'], imports: ['org.innapypa.Test'])
		new TestGenerator(properties).generate()
	}
}