package io.codearte.accurest

import groovy.transform.PackageScope
import groovy.util.logging.Slf4j
import io.codearte.accurest.builder.ClassBuilder
import io.codearte.accurest.config.AccurestConfigProperties
import io.codearte.accurest.config.TestFramework
import io.codearte.accurest.config.TestMode
import io.codearte.accurest.file.Contract

import static io.codearte.accurest.builder.ClassBuilder.createClass
import static io.codearte.accurest.builder.MethodBuilder.createTestMethod
import static io.codearte.accurest.util.NamesUtil.capitalize

@Slf4j
class SingleTestGenerator {

	private static
	final String JSON_ASSERT_STATIC_IMPORT = 'com.blogspot.toomuchcoding.jsonassert.JsonAssertion.assertThatJson'
	private static final String JSON_ASSERT_CLASS = 'com.blogspot.toomuchcoding.jsonassert.JsonAssertion'

	private final AccurestConfigProperties configProperties

	SingleTestGenerator(AccurestConfigProperties configProperties) {
		this.configProperties = configProperties
	}

	@PackageScope
	String buildClass(Collection<Contract> listOfFiles, String className, String classPackage) {
		ClassBuilder clazz = createClass(capitalize(className), classPackage, configProperties)

		if (configProperties.imports) {
			configProperties.imports.each {
				clazz.addImport(it)
			}
		}

		if (listOfFiles.ignored.find { it }) {
			clazz.addImport(configProperties.targetFramework.getIgnoreClass())
		}

		if (configProperties.staticImports) {
			configProperties.staticImports.each {
				clazz.addStaticImport(it)
			}
		}

		if (isScenarioClass(listOfFiles)) {
			clazz.addImport(configProperties.targetFramework.getOrderAnnotationImport())
			clazz.addClassLevelAnnotation(configProperties.targetFramework.getOrderAnnotation())
		}

		if (configProperties.testMode == TestMode.JAXRSCLIENT) {
			clazz.addStaticImport('javax.ws.rs.client.Entity.*')
		} else if (configProperties.testMode == TestMode.MOCKMVC) {
			clazz.addStaticImport('com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.*')
		} else {
			clazz.addStaticImport('com.jayway.restassured.RestAssured.*')
		}

		if (configProperties.targetFramework == TestFramework.JUNIT) {
			clazz.addImport('org.junit.Test')
		}

		if (configProperties.ruleClassForTests) {
			clazz.addImport('org.junit.Rule').addRule(configProperties.ruleClassForTests)
		}

		addJsonPathRelatedImports(clazz)

		listOfFiles.each {
			clazz.addMethod(createTestMethod(it, configProperties))
		}
		return clazz.build()
	}

	private boolean isScenarioClass(Collection<Contract> listOfFiles) {
		listOfFiles.find({ it.order != null }) != null
	}

	private ClassBuilder addJsonPathRelatedImports(ClassBuilder clazz) {
		clazz.addImport(['com.jayway.jsonpath.DocumentContext',
		                 'com.jayway.jsonpath.JsonPath'])
		if (jsonAssertPresent()) {
			clazz.addStaticImport(JSON_ASSERT_STATIC_IMPORT)
		}
	}

	private static boolean jsonAssertPresent() {
		try {
			Class.forName(JSON_ASSERT_CLASS)
			return true
		} catch (ClassNotFoundException e) {
			log.debug("JsonAssert is not present on classpath. Will not add a static import.")
			return false
		}
	}

}
