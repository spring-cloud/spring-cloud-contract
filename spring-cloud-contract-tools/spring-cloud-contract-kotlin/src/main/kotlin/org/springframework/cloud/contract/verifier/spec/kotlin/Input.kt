package org.springframework.cloud.contract.verifier.spec.kotlin

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.internal.Input

class Input(contract: Contract) {

    init {
        contract.input = Input()
    }
}