package org.springframework.cloud.contract.verifier.spec.kotlin

import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.ContractConverter
import org.springframework.cloud.contract.spec.KContract
import java.io.File

/**
 * Converter of Kotlin Dsl file
 *
 * @author Stephan Oudmaijer
 * @since 2.0.0
 */
open class KContractConverter : ContractConverter<KContract> {

    companion object {
        private val engine = KotlinJsr223JvmLocalScriptEngineFactory().scriptEngine
    }

    override fun convertTo(contracts: Collection<Contract>): KContract {
        return KContract() // TODO
    }

    override fun isAccepted(file: File) = "kts" == file.extension

    override fun convertFrom(file: File): Collection<Contract> {
        val eval = file.reader().use {
            engine.eval(it)
        }
        return when (eval) {
            is KContract -> listOf(eval.contract)
            is Iterable<*> -> eval.filterIsInstance(KContract::class.java).map { it.contract }
            is Array<*> -> eval.filterIsInstance(KContract::class.java).map { it.contract }
            else -> emptyList()
        }
    }
}