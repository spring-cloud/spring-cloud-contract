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
import javax.script.ScriptEngineManager

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
            file.reader().use {
                // Get a new engine every time we need to process a file.
                // Reusing the script engine could leak context and will fail subsequent evals
                ScriptEngineManager().getEngineByExtension(ext).eval(it)
            }
        }
        val contracts = when (eval) {
            is Contract -> listOf(eval)
            is Iterable<*> -> eval.filterIsInstance(Contract::class.java)
            is Array<*> -> eval.filterIsInstance(Contract::class.java)
            else -> emptyList()
        }

        return withName(file, contracts)
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
}