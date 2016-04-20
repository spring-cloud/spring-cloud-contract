package io.codearte.accurest

import groovy.transform.Canonical
import groovy.transform.PackageScope
import groovy.util.logging.Slf4j
import io.codearte.accurest.builder.ClassBuilder
import io.codearte.accurest.config.AccurestConfigProperties
import io.codearte.accurest.config.TestFramework
import io.codearte.accurest.config.TestMode
import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.file.Contract
import org.codehaus.groovy.control.CompilerConfiguration

import static io.codearte.accurest.builder.ClassBuilder.createClass
import static io.codearte.accurest.builder.MethodBuilder.createTestMethod
import static io.codearte.accurest.util.NamesUtil.capitalize

@Slf4j
class SingleTestGenerator {

	private static final String JSON_ASSERT_STATIC_IMPORT = 'com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson'
	private static final String JSON_ASSERT_CLASS = 'com.toomuchcoding.jsonassert.JsonAssertion'

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

		addJsonPathRelatedImports(clazz)

		Map<ParsedDsl, TestType> contracts = listOfFiles.collectEntries {
			File stubsFile = it.path.toFile()
			log.debug("Stub content from file [${stubsFile.text}]")
			GroovyDsl stubContent = new GroovyShell(delegate.class.classLoader, new Binding(), new CompilerConfiguration(sourceEncoding:'UTF-8')).evaluate(stubsFile)
			TestType testType = (stubContent.inputMessage || stubContent.outputMessage) ? TestType.MESSAGING : TestType.HTTP
			return [(new ParsedDsl(it, stubContent, stubsFile)) : testType]
		}

		boolean conditionalImportsAdded = false
		contracts.each { ParsedDsl key, TestType value ->
			if (!conditionalImportsAdded) {
				if (value == TestType.HTTP) {
					if (configProperties.testMode == TestMode.JAXRSCLIENT) {
						clazz.addStaticImport('javax.ws.rs.client.Entity.*')
						if (configProperties.targetFramework == TestFramework.JUNIT) {
							clazz.addImport('javax.ws.rs.core.Response')
						}
					} else if (configProperties.testMode == TestMode.MOCKMVC) {
						clazz.addStaticImport('com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.*')
					} else {
						clazz.addStaticImport('com.jayway.restassured.RestAssured.*')
					}
				}
				if (configProperties.targetFramework == TestFramework.JUNIT) {
					if (value == TestType.HTTP && configProperties.testMode == TestMode.MOCKMVC) {
						clazz.addImport('com.jayway.restassured.module.mockmvc.specification.MockMvcRequestSpecification')
						clazz.addImport('com.jayway.restassured.response.ResponseOptions')
					}
					clazz.addImport('org.junit.Test')
					clazz.addStaticImport('org.assertj.core.api.Assertions.assertThat')
				}
				if (configProperties.ruleClassForTests) {
					clazz.addImport('org.junit.Rule').addRule(configProperties.ruleClassForTests)
				}
				if (value == TestType.MESSAGING) {
					addMessagingRelatedEntries(clazz)
				}
				conditionalImportsAdded = true
			}
			clazz.addMethod(createTestMethod(key.contract, key.stubsFile, key.groovyDsl, configProperties))
		}
		return clazz.build()
	}

	@Canonical
	private static class ParsedDsl {
		Contract contract
		GroovyDsl groovyDsl
		File stubsFile
	}

	private static enum TestType {
		MESSAGING, HTTP
	}

	private boolean isScenarioClass(Collection<Contract> listOfFiles) {
		listOfFiles.find({ it.order != null }) != null
	}

	private ClassBuilder addJsonPathRelatedImports(ClassBuilder clazz) {
		clazz.addImport(['com.jayway.jsonpath.DocumentContext',
		                 'com.jayway.jsonpath.JsonPath',
		])
		if (jsonAssertPresent()) {
			clazz.addStaticImport(JSON_ASSERT_STATIC_IMPORT)
		}
	}

	private ClassBuilder addMessagingRelatedEntries(ClassBuilder clazz) {
		clazz.addField(['@Inject AccurestMessaging accurestMessaging',
						'ObjectMapper accurestObjectMapper = new ObjectMapper()'
		])
		clazz.addImport([ 'javax.inject.Inject',
						  'com.fasterxml.jackson.databind.ObjectMapper',
						  'io.codearte.accurest.messaging.AccurestMessage',
						  'io.codearte.accurest.messaging.AccurestMessaging',
		])
		clazz.addStaticImport('io.codearte.accurest.messaging.AccurestMessagingUtil.headers')
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
