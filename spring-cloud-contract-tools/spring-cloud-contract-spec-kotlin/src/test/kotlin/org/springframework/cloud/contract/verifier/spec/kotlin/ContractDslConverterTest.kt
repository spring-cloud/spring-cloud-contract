package org.springframework.cloud.contract.spec

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.springframework.cloud.contract.verifier.spec.kotlin.ContractDslConverter
import java.io.File

class ContractDslConverterTest {

    @Test
    fun `convert Contract to KContract`() {
        val file = file("contracts/shouldMarkClientAsFraud.groovy")
        val contracts = ContractDslConverter().convertTo(listOf(Contract()))
        assertEquals(1, contracts.size)
    }

    @Test
    fun `accept kts files`() {
        assertTrue(ContractDslConverter().isAccepted(file("contracts/shouldMarkClientAsFraud.kts")))
    }

    @Test
    fun `convert from with single contract definition`() {
        val converter = ContractDslConverter()
        val contracts = converter.convertFrom(file("contracts/shouldMarkClientAsFraud.kts"))
        assertEquals(1, contracts.size)
    }

    @Test
    fun `convert from with 2 elements array`() {
        val converter = ContractDslConverter()
        val contracts = converter.convertFrom(file("contracts/shouldReturnFraudStats.kts"))
        assertEquals(2, contracts.size)
    }

    private fun file(filename: String) = File(this.javaClass.classLoader.getResource(filename).toURI())
}