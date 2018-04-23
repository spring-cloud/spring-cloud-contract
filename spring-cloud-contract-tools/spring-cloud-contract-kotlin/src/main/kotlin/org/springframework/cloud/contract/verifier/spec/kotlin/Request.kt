package org.springframework.cloud.contract.verifier.spec.kotlin

import org.springframework.cloud.contract.spec.internal.Body
import org.springframework.cloud.contract.spec.internal.HttpMethods.HttpMethod
import org.springframework.cloud.contract.spec.internal.Url
import org.springframework.cloud.contract.spec.internal.Request as DelegateRequest


open class Request(val delegate: DelegateRequest) {

    var headers = Headers()

    fun method(value: String) {
        delegate.method(value)
    }

    fun method(httpMethod: HttpMethod) {
        // this.method = delegate.toDslProperty(httpMethod.toString())
    }

    fun url(url: String) {
        delegate.url = Url(url)
    }

    fun body(vararg pairs: Pair<String, Any>) {
        delegate.body = Body(delegate.convertObjectsToDslProperties(pairs.toMap()))
    }

    fun body(pair: Pair<String, Any>) {
        delegate.body = Body(delegate.convertObjectsToDslProperties(mapOf(pair)))
    }

    fun body(value: String) {
        delegate.body = Body(value)
    }

    //    fun url(Object url, @DelegatesTo(UrlPath) Closure closure) {
//        this.url = new Url(url)
//        closure.delegate = this.url
//        closure()
//    }
//
//    fun url(DslProperty url, @DelegatesTo(UrlPath) Closure closure) {
//        this.url = new Url(url)
//        closure.delegate = this.url
//        closure()
//    }
//
//    fun urlPath(String path) {
//        this.urlPath = new UrlPath(path)
//    }
//
//    fun urlPath(DslProperty path) {
//        this.urlPath = new UrlPath(path)
//    }
//
//    fun urlPath(String path, @DelegatesTo(UrlPath) Closure closure) {
//        this.urlPath = new UrlPath(path)
//        closure.delegate = urlPath
//        closure()
//    }
//
//    fun urlPath(DslProperty path, @DelegatesTo(UrlPath) Closure closure) {
//        this.urlPath = new UrlPath(path)
//        closure.delegate = urlPath
//        closure()
//    }
//
    fun headers(init: Headers.() -> Unit): Headers {
        this.headers = Headers()
        headers.init()
        return headers

    }
//
//    fun body(Map<String, Object> body) {
//        this.body = new Body(convertObjectsToDslProperties(body))
//    }
//
//    fun body(List body) {
//        this.body = new Body(convertObjectsToDslProperties(body))
//    }
//
//    fun body(DslProperty dslProperty) {
//        this.body = new Body(dslProperty)
//    }
//
//    fun body(Object bodyAsValue) {
//        this.body = new Body(bodyAsValue)
//    }
//
//    fun getBody() : Body  {
//        return body
//    }
//
//    fun  multipart(Map<String, Object> body) {
//        this.multipart = new Multipart(convertObjectsToDslProperties(body))
//    }
//
//    fun multipart(List multipartAsList) {
//        this.multipart = new Multipart(convertObjectsToDslProperties(multipartAsList))
//    }
//
//    fun multipart(DslProperty dslProperty) {
//        this.multipart = new Multipart(dslProperty)
//    }
//
//    fun multipart(Object multipartAsValue) {
//        this.multipart = new Multipart(multipartAsValue)
//    }

}
