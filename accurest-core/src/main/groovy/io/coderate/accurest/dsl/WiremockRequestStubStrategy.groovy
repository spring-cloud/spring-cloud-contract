package io.coderate.accurest.dsl
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import groovy.xml.XmlUtil
import io.coderate.accurest.dsl.internal.ClientRequest
import io.coderate.accurest.dsl.internal.Request

import java.util.regex.Pattern

import static groovy.json.StringEscapeUtils.escapeJava

@TypeChecked
@PackageScope
class WiremockRequestStubStrategy extends BaseWiremockStubStrategy {

	private final Request request

	WiremockRequestStubStrategy(GroovyDsl groovyDsl) {
		this.request = groovyDsl.request
	}

	@PackageScope
	Map buildClientRequestContent() {
		return buildRequestContent(new ClientRequest(request))
	}

	private Map<String, Object> buildRequestContent(ClientRequest request) {
		return ([method    : request?.method?.clientValue,
		        headers   : buildClientRequestHeadersSection(request.headers)
		] << appendUrl(request) << appendBody(request)).findAll { it.value }
	}

	private Map<String, Object> appendUrl(ClientRequest clientRequest) {
		Object url = clientRequest?.url?.clientValue
		return url instanceof Pattern ? [urlPattern: ((Pattern)url).pattern()] : [url: url]
	}

	private Map<String, Object> appendBody(ClientRequest clientRequest) {
		Object body = clientRequest?.body?.clientValue
		return body != null ? [bodyPatterns: [equalTo: parseBody(body)]] : [:]
	}

	private String parseBody(Object responseBodyObject) {
		String responseBody = responseBodyObject as String
		try {
			def json = new JsonSlurper().parseText(responseBody)
			return escapeJava(JsonOutput.toJson(responseBody))
		} catch (Exception jsonException) {
			try {
				def xml = new XmlSlurper().parseText(responseBody)
				return escapeJava(XmlUtil.serialize(responseBody))
			} catch (Exception xmlException) {
				return escapeJava(responseBody)
			}
		}
	}

	private String parseBody(List responseBody) {
		return JsonOutput.toJson(responseBody)
	}

	private String parseBody(Map responseBody) {
		return JsonOutput.toJson(responseBody)
	}

}
