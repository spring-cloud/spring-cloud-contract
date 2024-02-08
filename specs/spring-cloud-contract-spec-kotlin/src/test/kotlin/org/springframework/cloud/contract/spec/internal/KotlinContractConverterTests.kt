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

import org.junit.jupiter.api.Test
import org.springframework.cloud.contract.spec.Contract
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KotlinContractConverterTests {

    @Test
    fun `accept kts files`() {
        assertTrue(KotlinContractConverter().isAccepted(file("contracts/singleDefinition.kts")))
    }

    @Test
    fun `should convert single contract definition`() {
        val converter = KotlinContractConverter()
        val contracts = converter.convertFrom(file("contracts/singleDefinition.kts"))
        assertEquals(1, contracts.size)
        contracts.forEach(Contract::assertContract)
    }

    @Test
    fun `should convert multiple contract definitions`() {
        val converter = KotlinContractConverter()
        val contracts = converter.convertFrom(file("contracts/multipleDefinitions.kts"))
        assertEquals(2, contracts.size)
        contracts.forEach(Contract::assertContract)
    }

    @Test
    fun `should work with binary payload`() {
        val converter = KotlinContractConverter()
        val contracts = converter.convertFrom(file("contracts/shouldWorkWithBinaryPayload.kts"))
        assertEquals(1, contracts.size)
        contracts.forEach(Contract::assertContract)
    }

    private fun file(filename: String) = File(javaClass.classLoader.getResource(filename)!!.toURI())
}