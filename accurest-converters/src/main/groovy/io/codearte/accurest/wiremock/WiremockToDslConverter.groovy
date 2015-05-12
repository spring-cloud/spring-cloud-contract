package io.codearte.accurest.wiremock
import groovy.io.FileType
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.xml.XmlUtil
import io.codearte.accurest.dsl.GroovyDsl
import nl.flotsam.xeger.Xeger

import static org.apache.commons.lang3.StringEscapeUtils.escapeJava

class WiremockToDslConverter {
	static String fromWiremockStub(String wiremockStringStub) {
		return new WiremockToDslConverter().convertFromWiremockStub(wiremockStringStub)
	}

	private String convertFromWiremockStub(String wiremockStringStub) {
		Object wiremockStub = new JsonSlurper().parseText(wiremockStringStub)
		def request = wiremockStub.request
		def response = wiremockStub.response
		def bodyPatterns = request.bodyPatterns
		String urlPattern = request.urlPattern
		return """\
			request {
				${request.method ? "method \"\"\"$request.method\"\"\"" : ""}
				${request.url ? "url \"\"\"$request.url\"\"\"" : ""}
				${urlPattern ? "url \$(client(regex('${escapeJava(urlPattern)}')), server('${new Xeger(escapeJava(urlPattern)).generate()}'))" : ""}
				${request.urlPath ? "url \"\"\"$request.urlPath\"\"\"" : ""}
				${
					request.headers ? """headers {
							${
						request.headers.collect {
							def assertion = it.value
							String headerName = it.key as String
							def entry = assertion.entrySet().first()
							"""header(\"\"\"$headerName\"\"\", ${buildHeader(entry.key, entry.value)})\n"""
						}.join('')
					}
						}
						""" : ""
				}
				${bodyPatterns?.equalTo?.every { it } ? "body('''${bodyPatterns.equalTo[0]}''')" : ''}
				${bodyPatterns?.equalToJson?.every { it } ? "body('''${bodyPatterns.equalToJson[0]}''')" : ''}
				${bodyPatterns?.matches?.every { it } ? "body \$(client(regex('${escapeJava(bodyPatterns.matches[0])}')), server('${new Xeger(escapeJava(bodyPatterns.matches[0])).generate()}'))" : ""}
			}
			response {
				${response.status ? "status $response.status" : ""}
				${response.body ? "body( ${buildBody(response.body)})" : ""}
				${
			response.headers ? """headers {
					 ${response.headers.collect { "header('$it.key': '${it.value}')\n" }.join('')}
					}
				""" : ""
		}
			}
		"""
	}

	private String buildHeader(String method, Object value) {
		switch (method) {
			case 'equalTo':
				return wrapWithMultilineGString(value)
			default:
				return "regex(${wrapWithMultilineGString(escapeJava(value as String))})"
		}
	}

	private Object buildBody(Map responseBody) {
		return responseBody.entrySet().collectAll(withQuotedMapStringElements()).inject([:], appendToIterable())
	}

	private Object buildBody(List responseBody) {
		return responseBody.collectAll(withQuotedStringElements()).inject([], appendToIterable())
	}

	private Object buildBody(Integer responseBody) {
		return responseBody
	}

	private Object buildBody(String responseBody) {
		try {
			def json = new JsonSlurper().parseText(responseBody)
			return wrapWithMultilineGString(JsonOutput.prettyPrint(responseBody))
		} catch (Exception jsonException) {
			try {
				def xml = new XmlSlurper().parseText(responseBody)
				return wrapWithMultilineGString(XmlUtil.serialize(responseBody))
			} catch (Exception xmlException) {
				return wrapWithMultilineGString(responseBody)
			}
		}
	}

	private String wrapWithMultilineGString(String string) {
		return """\"\"\"$string\"\"\""""
	}

	private Closure withQuotedMapStringElements() {
		return {
			[(it.key): convert(it.value)]
		}
	}

	private Closure withQuotedStringElements() {
		return {
			convert(it)
		}
	}

	private Closure appendToIterable() {
		return {
			acc, el -> acc << el
		}
	}

	private Object convert(Object element) {
		return element
	}

	private Object convert(String element) {
		return quoteString(element)
	}

	private String quoteString(String element) {
		if (element =~ /^".*"$/) {
			return element
		}
		return """\"\"\"${escapeJava(element)}\"\"\""""
	}

	private Object convert(List element) {
		return element.collect {
			convert(it)
		}
	}

	private Object convert(Map element) {
		return element.collectEntries {
			[(it.key): convert(it.value)]
		}
	}

	static void main(String[] args) {
		String rootOfFolderWithStubs = args[0]
		new File(rootOfFolderWithStubs).eachFileRecurse(FileType.FILES) {
			try {
				if (!it.name.endsWith('json')) {
					return
				}
				String dslFromWiremockStub = fromWiremockStub(it.text)
				String dslWrappedWithFactoryMethod = wrapWithFactoryMethod(dslFromWiremockStub)
				File newGroovyFile = new File(it.parent, it.name.replaceAll('json', 'groovy'))
				println("Creating new groovy file [$newGroovyFile.path]")
				newGroovyFile.text = dslWrappedWithFactoryMethod
			} catch (Exception e) {
				System.err.println(e)
			}

		}
	}

	static String wrapWithFactoryMethod(String dslFromWiremockStub) {
		return """\
${GroovyDsl.name}.make {
	$dslFromWiremockStub
}
"""
	}
}
