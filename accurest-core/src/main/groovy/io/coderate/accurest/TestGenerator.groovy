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
import static io.coderate.accurest.util.NamesUtil.afterLast
import static io.coderate.accurest.util.NamesUtil.capitalize

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
		generateTestClasses(new File(stubsBaseDirectory), configProperties.basePackageForTests)
	}

	protected void generateTestClasses(File baseFile, String packageName) {
		List<File> files = baseFile.listFiles()

		files.each {
			if (it.isDirectory()) {
				generateTestClasses(it, "$packageName.$it.name")
				if (containsStubs(it)) {
					def testBaseDir = Paths.get(targetDirectory, NamesUtil.packageToDirectory(packageName))
					Files.createDirectories(testBaseDir)
					Files.write(Paths.get(testBaseDir.toString(), capitalize(it.name) + getTestClassExtension()),
							buildClass(it, packageName).bytes)
				}
			}
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

	private String buildClass(File directory, String classPackage) {
		ClassBuilder clazz = createClass(capitalize(afterLast(directory.path, '/')), classPackage, configProperties)

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

		directory.listFiles().grep({ File file -> !file.isDirectory()}).each {
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