package org.springframework.cloud.contract.verifier.spec.kotlin

import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.ContractConverter
import org.springframework.cloud.contract.spec.ContractDsl
import java.io.File

/**
 * Converter for Kotlin script Dsl file(s)
 *
 * @author Stephan Oudmaijer
 * @since 2.0.0
 */
open class ContractDslConverter : ContractConverter<List<ContractDsl>> {

    companion object {
        private val engine = KotlinJsr223JvmLocalScriptEngineFactory()
    }

    override fun convertTo(contracts: Collection<Contract>) = contracts.map { ContractDsl(it) }

    override fun isAccepted(file: File) = "kts" == file.extension

    override fun convertFrom(file: File): Collection<Contract> {
        val eval = file.reader().use {
            engine.scriptEngine.eval(it)
        }
        return when (eval) {
            is ContractDsl -> listOf(eval.contract)
            is Iterable<*> -> eval.filterIsInstance(ContractDsl::class.java).map { it.contract }
            is Array<*> -> eval.filterIsInstance(ContractDsl::class.java).map { it.contract }
            else -> emptyList()
        }
    }
}