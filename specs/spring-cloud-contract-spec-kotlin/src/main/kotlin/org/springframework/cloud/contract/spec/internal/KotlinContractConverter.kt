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

package org.springframework.cloud.contract.spec.internal

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.ContractConverter
import java.io.File
import javax.script.ScriptEngineManager

/**
 * @author Tim Ysewyn
 */
class KotlinContractConverter: ContractConverter<List<Contract>> {

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
        val eval = file.reader().use {
            // Get a new engine every time we need to process a file.
            // Reusing the script engine could leak context and will fail subsequent evals
            ScriptEngineManager().getEngineByExtension(ext).eval(it)
        }
        return when (eval) {
            is Contract -> listOf(eval)
            is Iterable<*> -> eval.filterIsInstance(Contract::class.java)
            is Array<*> -> eval.filterIsInstance(Contract::class.java)
            else -> emptyList()
        }
    }

    override fun convertTo(contract: Collection<Contract>) = contract.toList()
}