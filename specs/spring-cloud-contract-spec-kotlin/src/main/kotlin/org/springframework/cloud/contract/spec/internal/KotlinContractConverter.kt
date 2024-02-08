/*
 * Copyright 2013-2020 the original author or authors.
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

package org.springframework.cloud.contract.spec.internal

import org.apache.commons.lang3.ObjectUtils
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.ContractConverter
import java.io.File
import java.net.URLClassLoader.newInstance
import java.util.concurrent.atomic.AtomicInteger
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

/**
 * Converter that will convert the Kotlin DSL to Java DSL.
 *
 * @author Tim Ysewyn
 * @since 2.2.0
 */
class KotlinContractConverter : ContractConverter<List<Contract>> {

	private val ext = "kts"

	constructor() {
		// Sets an {@code idea.use.native.fs.for.win} system property to {@code false}
		// to disable a native engine discovery for Windows: may be resolved in the future Kotlin versions.
		System.setProperty("idea.use.native.fs.for.win", "false")
	}

	override fun isAccepted(file: File): Boolean {
		return ext == file.extension
	}

	override fun convertFrom(file: File): Collection<Contract> {
		val eval = withUpdatedClassloader(file) {
			BasicJvmScriptingHost().eval(file.toScriptSource(), ScriptWithCurrentClasspathConfiguration(), null)
		}
		when (eval) {
			is ResultWithDiagnostics.Success<*> -> {
				val contracts = when (val parsedValue = ((eval.value as EvaluationResult).returnValue as ResultValue.Value).value) {
					is Contract -> listOf(parsedValue)
					is Iterable<*> -> parsedValue.filterIsInstance(Contract::class.java)
					is Array<*> -> parsedValue.filterIsInstance(Contract::class.java)
					else -> emptyList()
				}

				return withName(file, contracts)
			}

			else -> throw IllegalStateException("Failed to parse kotlin script due to ${(eval as ResultWithDiagnostics.Failure).reports}")
		}
	}

	private fun withName(file: File, contracts: Collection<Contract>): Collection<Contract> {
		val counter = AtomicInteger(0)
		return contracts.onEach { contract ->
			if (ObjectUtils.isEmpty(contract.name)) {
				contract.name = defaultContractName(file, contracts, counter.get())
			}
			counter.incrementAndGet()
		}
	}

	private fun defaultContractName(file: File, contracts: Collection<*>, counter: Int): String {
		val lastIndexOfDot = file.name.lastIndexOf(".")
		val tillExtension = file.name.substring(0, lastIndexOfDot)
		return tillExtension + if (counter > 0 || contracts.size > 1) "_$counter" else ""
	}

	override fun convertTo(contract: Collection<Contract>) = contract.toList()

	private fun withUpdatedClassloader(file: File, block: ClassLoader.() -> Any): Any {
		val currentClassLoader = Thread.currentThread().contextClassLoader
		try {
			val tempClassLoader = newInstance(arrayOf(file.parentFile.toURI().toURL()), currentClassLoader)
			Thread.currentThread().contextClassLoader = tempClassLoader
			return tempClassLoader.block()
		} finally {
			Thread.currentThread().contextClassLoader = currentClassLoader
		}
	}

	class ScriptWithCurrentClasspathConfiguration : ScriptCompilationConfiguration(
		{
			jvm {
				// Extract the whole classpath from context classloader and use it as dependencies
				dependenciesFromCurrentContext(wholeClasspath = true)
			}
		}
	)
}
