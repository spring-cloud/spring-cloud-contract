package org.springframework.cloud.contract.verifier.spec.kotlin

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class KotlinContractConverterTest {

    @Test
    fun convertTo() {
        // TODO
    }

    @Test
    fun `accept kts files`() {
        assertTrue(KotlinContractConverter().isAccepted(file("contracts/shouldMarkClientAsFraud.kts")))
    }

    @Test
    fun `convert from with single contract definition`() {
        val converter = KotlinContractConverter()
        val contracts = converter.convertFrom(file("contracts/shouldMarkClientAsFraud.kts"))
        assertEquals(1, contracts.size)
    }

    @Test
    fun `convert from with 2 elements array`() {
        val converter = KotlinContractConverter()
        val contracts = converter.convertFrom(file("contracts/shouldReturnFraudStats.kts"))
        assertEquals(2, contracts.size)
    }

    private fun file(filename: String) = File(this.javaClass.classLoader.getResource(filename).toURI())
}