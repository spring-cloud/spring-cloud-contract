package org.springframework.cloud.contract.verifier.spec.kotlin

import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.ContractConverter
import org.springframework.cloud.contract.spec.KotlinContract
import java.io.File

/**
 * Converter of Kotlin Dsl file
 *
 * @author Stephan Oudmaijer
 * @since 2.0.0
 */
open class KotlinContractConverter : ContractConverter<KotlinContract> {

    val engine = KotlinJsr223JvmLocalScriptEngineFactory().getScriptEngine()

    override fun convertTo(contract: Collection<Contract>): KotlinContract {
        TODO("not implemented")
    }

    override fun isAccepted(file: File) = file.extension.endsWith(".kts")

    override fun convertFrom(file: File): Collection<Contract> {
        // TODO eval
        return listOf()
    }
}