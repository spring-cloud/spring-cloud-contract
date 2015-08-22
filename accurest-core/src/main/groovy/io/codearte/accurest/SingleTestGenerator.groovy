package io.codearte.accurest

import groovy.transform.PackageScope
import io.codearte.accurest.builder.ClassBuilder
import io.codearte.accurest.config.AccurestConfigProperties
import io.codearte.accurest.config.TestFramework
import io.codearte.accurest.config.TestMode

import static io.codearte.accurest.builder.ClassBuilder.createClass
import static io.codearte.accurest.builder.MethodBuilder.createTestMethod
import static io.codearte.accurest.util.NamesUtil.capitalize

class SingleTestGenerator {

	private final AccurestConfigProperties configProperties

	SingleTestGenerator(AccurestConfigProperties configProperties) {
		this.configProperties = configProperties
	}

	@PackageScope
	String buildClass(List<File> listOfFiles, String className, String classPackage) {
		ClassBuilder clazz = createClass(capitalize(className), classPackage,
				configProperties)

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

		if (configProperties.testMode == TestMode.JAXRSCLIENT) {
			clazz.addStaticImport('javax.ws.rs.client.Entity.*')
		} else if (configProperties.testMode == TestMode.MOCKMVC) {
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

		addJsonPathRelatedImports(clazz)

		listOfFiles.each {
			clazz.addMethod(createTestMethod(it, configProperties))
		}
		return clazz.build()
	}

	private ClassBuilder addJsonPathRelatedImports(ClassBuilder clazz) {
		clazz.addImport(['com.jayway.jsonpath.DocumentContext',
						 'com.jayway.jsonpath.JsonPath',
						 'net.minidev.json.JSONArray'])
	}

}
