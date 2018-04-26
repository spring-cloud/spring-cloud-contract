package org.springframework.cloud.contract.verifier.spec.kotlin

class DslProperty<T>(val clientValue: T?,
                     val serverValue: T? = clientValue) {

    fun isSingleValue() = this.clientValue == this.serverValue ||
            (this.clientValue != null && this.serverValue == null) ||
            (this.serverValue != null && this.clientValue == null)
}