/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties
import org.springframework.cloud.contract.verifier.config.TestFramework
import org.springframework.cloud.contract.verifier.config.TestMode
import org.springframework.cloud.contract.verifier.plugin.ContractVerifierExtension

plugins {
	groovy
	id("org.springframework.boot") apply false
	id("org.springframework.cloud.contract") apply false
}

val restAssuredVersion by extra("3.0.7")
val spockVersion by extra("1.0-groovy-2.4")
val wiremockVersion: String by extra
val jsonAssertVersion: String by extra
val verifierVersion: String by extra

val contractVerifierStubsBaseDirectory by extra("src/test/resources/stubs")

group = "org.springframework.cloud.testprojects"

subprojects {
	apply(plugin = "groovy")

	java {
		sourceCompatibility = JavaVersion.VERSION_1_8
		targetCompatibility = JavaVersion.VERSION_1_8
	}

	repositories {
		mavenCentral()
		mavenLocal()
		maven(url = "https://repo.spring.io/snapshot")
		maven(url = "https://repo.spring.io/milestone")
		maven(url = "https://repo.spring.io/release")
	}

	dependencies {
		testCompile("org.codehaus.groovy:groovy")
		testCompile("org.spockframework:spock-core:$spockVersion")
		testCompile("junit:junit:4.12")
		testCompile("com.github.tomakehurst:wiremock:$wiremockVersion")
		testCompile("com.toomuchcoding.jsonassert:jsonassert:$jsonAssertVersion")
		testCompile("org.springframework.cloud:spring-cloud-contract-verifier:$verifierVersion")
	}
}

configure(listOf(project(":fraudDetectionService"), project(":loanApplicationService"))) {
	apply(plugin = "org.springframework.boot")
	apply(plugin = "io.spring.dependency-management")
	apply(plugin = "org.springframework.cloud.contract")
	apply(plugin = "maven-publish")

	configure<ContractVerifierExtension> {
		contractsMode.set(StubRunnerProperties.StubsMode.REMOTE)
		testFramework.set(TestFramework.SPOCK)
		testMode.set(TestMode.MOCKMVC)
		baseClassForTests.set("org.springframework.cloud.MvcSpec")
		contractsDslDir.set(file("${project.projectDir}/mappings/"))
		generatedTestSourcesDir.set(file("${project.buildDir}/generated-test-sources/"))
		stubsOutputDir.set(file("${project.buildDir}/production/${project.name}-stubs/"))
	}

	dependencies {
		compile("org.springframework.boot:spring-boot-starter-web") {
			exclude(module = "spring-boot-starter-tomcat")
		}
		compile("org.springframework.boot:spring-boot-starter-jetty")
		compile("org.springframework.boot:spring-boot-starter-actuator")

		testRuntime("org.spockframework:spock-spring:$spockVersion") {
			exclude(group = "org.codehaus.groovy")
		}
		testCompile("org.mockito:mockito-core")
		testCompile("org.springframework:spring-test")
		testCompile("org.springframework.boot:spring-boot-test")
		testCompile("io.rest-assured:rest-assured:$restAssuredVersion")
		testCompile("io.rest-assured:spring-mock-mvc:$restAssuredVersion")
	}

	tasks {
		jar {
			version = "0.0.1"
		}

		test {
			testLogging.showExceptions = true
		}

		val cleanup by registering(Delete::class) {
			delete("src/test/resources/mappings", "src/test/resources/stubs")
		}

		clean {
			dependsOn(cleanup)
		}
	}
}

configure(listOf(project(":fraudDetectionService"))) {
	tasks {
		test {
			dependsOn("generateClientStubs")
		}
	}
}

configure(listOf(project(":loanApplicationService"))) {
	tasks {
		val copyCollaboratorStubs by registering(Copy::class) {
			val fraudDetectionService = project(":fraudDetectionService")
			from(file("${fraudDetectionService.buildDir}/production/${fraudDetectionService.name}-stubs"))
			into("src/test/resources/mappings")
		}

		val generateContractTests by existing
		generateContractTests {
			dependsOn(copyCollaboratorStubs)
		}
	}
}
