package io.codearte.accurest.dsl.internal

import spock.lang.Specification
import spock.lang.Unroll

import java.util.regex.Pattern

class RegexPatternsSpec extends Specification {

	RegexPatterns regexPatterns = new RegexPatterns()

	@Unroll
	def "should generate a regex for ip address [#textToMatch] that is a match [#shouldMatch]"() {
		expect:
			shouldMatch == Pattern.compile(regexPatterns.ipAddress()).matcher(textToMatch).matches()
		where:
			textToMatch       || shouldMatch
			'123.123.123.123' || true
			'a.b.'            || false
	}

	@Unroll
	def "should generate a regex for hostname [#textToMatch] that is a match [#shouldMatch]"() {
		expect:
			shouldMatch == Pattern.compile(regexPatterns.hostname()).matcher(textToMatch).matches()
		where:
			textToMatch            || shouldMatch
			'https://asd.com'      || true
			'https://asd.com:8080' || true
			'https://asd.com/asd'  || false
			'asd.com'              || false
	}

	@Unroll
	def "should generate a regex for email [#textToMatch] that is a match [#shouldMatch]"() {
		expect:
			shouldMatch == Pattern.compile(regexPatterns.email()).matcher(textToMatch).matches()
		where:
			textToMatch   || shouldMatch
			'asd@asd.com' || true
			'a.b.'        || false
	}

	@Unroll
	def "should generate a regex for url [#textToMatch] that is a match [#shouldMatch]"() {
		expect:
			shouldMatch == Pattern.compile(regexPatterns.url()).matcher(textToMatch).matches()
		where:
			textToMatch                    || shouldMatch
			'ftp://asd.com:9090/asd/a?a=b' || true
			'a.b.'                         || false
	}
}
