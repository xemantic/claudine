/*
 * claudine - an autonomous and Unix-omnipotent AI agent using Anthropic API
 * Copyright (C) 2025 Kazimierz Pogoda / Xemantic
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.xemantic.ai.claudine.tool

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertContains

class MemoryTest {

    private val testKey = "test_key_${System.currentTimeMillis()}"
    private val testValue = "test value"

    @Test
    fun testMemoryOperations() {
        // Test store operation
        val storeMemory = Memory(
            purpose = "Testing memory store",
            operation = "store",
            key = testKey,
            value = testValue
        )
        val storeResult = storeMemory.use()
        assertContains(storeResult, "Successfully stored")
        
        // Test retrieve operation
        val retrieveMemory = Memory(
            purpose = "Testing memory retrieve",
            operation = "retrieve",
            key = testKey
        )
        val retrieveResult = retrieveMemory.use()
        assertEquals(testValue, retrieveResult)
        
        // Test list operation
        val listMemory = Memory(
            purpose = "Testing memory list",
            operation = "list"
        )
        val listResult = listMemory.use()
        assertContains(listResult, testKey)
        
        // Test delete operation
        val deleteMemory = Memory(
            purpose = "Testing memory delete",
            operation = "delete",
            key = testKey
        )
        val deleteResult = deleteMemory.use()
        assertContains(deleteResult, "Successfully deleted")
        
        // Verify deletion
        val verifyDeletedMemory = Memory(
            purpose = "Verifying memory deletion",
            operation = "retrieve",
            key = testKey
        )
        val verifyDeletedResult = verifyDeletedMemory.use()
        assertContains(verifyDeletedResult, "No memory found")
    }
    
    @Test
    fun testInvalidOperations() {
        // Test invalid operation
        val invalidOperation = Memory(
            purpose = "Testing invalid operation",
            operation = "invalidOp"
        )
        val invalidOpResult = invalidOperation.use()
        assertContains(invalidOpResult, "Unknown operation")
        
        // Test store with missing key
        val missingKeyStore = Memory(
            purpose = "Testing store with missing key",
            operation = "store",
            value = "test"
        )
        val missingKeyStoreResult = missingKeyStore.use()
        assertContains(missingKeyStoreResult, "Error")
        
        // Test store with missing value
        val missingValueStore = Memory(
            purpose = "Testing store with missing value",
            operation = "store",
            key = "test_key"
        )
        val missingValueStoreResult = missingValueStore.use()
        assertContains(missingValueStoreResult, "Error")
    }
}