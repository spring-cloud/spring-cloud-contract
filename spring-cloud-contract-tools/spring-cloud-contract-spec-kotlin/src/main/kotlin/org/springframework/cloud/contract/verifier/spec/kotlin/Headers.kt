package org.springframework.cloud.contract.verifier.spec.kotlin

import com.github.tomakehurst.wiremock.client.WireMock.matching
import org.springframework.cloud.contract.spec.internal.Header
import org.springframework.cloud.contract.spec.internal.HttpHeaders
import org.springframework.cloud.contract.spec.internal.MediaTypes
import org.springframework.cloud.contract.spec.internal.MessagingHeaders

open class Headers {

    val mediaTypes = MediaTypes()
    val httpHeaders = HttpHeaders()
    val messagingHeaders = MessagingHeaders()
    val entries = mutableSetOf<Header>()


    fun header(headerKey: String, headerValue: Any) {
        entries += Header(headerKey, headerValue)
    }

    fun accept(contentType: String) {
        header(HttpHeaders().accept(), matching(contentType))
    }

    fun contentType(): String = httpHeaders.contentType()

    fun contentType(contentType: String) {
        header(httpHeaders.contentType(), matching(contentType))
    }

    fun messagingContentType(contentType: String) {
        header(messagingHeaders.messagingContentType(), matching(contentType))
    }
}