/*
 *  Copyright 2013-2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.spec.internal

import spock.lang.Specification

import java.util.regex.Pattern

class RegexPatternsSpec extends Specification {

	RegexPatterns regexPatterns = new RegexPatterns()

	def "should generate a regex for ip address [#textToMatch] that is a match [#shouldMatch]"() {
		expect:
			shouldMatch == Pattern.compile(regexPatterns.ipAddress()).matcher(textToMatch).matches()
		where:
			textToMatch       || shouldMatch
			'123.123.123.123' || true
			'a.b.'            || false
	}

	def "should generate a regex for hostname [#textToMatch] that is a match [#shouldMatch]"() {
		expect:
			shouldMatch == Pattern.compile(regexPatterns.hostname()).matcher(textToMatch).matches()
		where:
			textToMatch              || shouldMatch
			'https://asd.com'        || true
			'https://asd.com:8080'   || true
			'https://localhost'      || true
			'https://localhost:8080' || true
			'https://asd.com/asd'    || false
			'asd.com'                || false
	}

	def "should generate a regex for email [#textToMatch] that is a match [#shouldMatch]"() {
		expect:
			shouldMatch == Pattern.compile(regexPatterns.email()).matcher(textToMatch).matches()
		where:
			textToMatch   || shouldMatch
			'asd@asd.com' || true
			'a.b.'        || false
	}

	def "should generate a regex for url [#textToMatch] that is a match [#shouldMatch]"() {
		expect:
			shouldMatch == Pattern.compile(regexPatterns.url()).matcher(textToMatch).matches()
		where:
			textToMatch                    || shouldMatch
			'ftp://asd.com:9090/asd/a?a=b' || true
			'a.b.'                         || false
	}

	def "should generate a regex for a number [#textToMatch] that is a match [#shouldMatch]"() {
		expect:
			shouldMatch == Pattern.compile(regexPatterns.number()).matcher(textToMatch).matches()
		where:
			textToMatch || shouldMatch
			'1'         || true
			'1.0'       || true
			'0.1'       || true
			'.1'        || true
			'1.'        || false
	}

	def "should generate a regex for a uuid [#textToMatch] that is a match [#shouldMatch]"() {
		expect:
			shouldMatch == Pattern.compile(regexPatterns.uuid()).matcher(textToMatch).matches()
		where:
		textToMatch							|| shouldMatch
			UUID.randomUUID().toString()   		|| true
			UUID.randomUUID().toString()   		|| true
			UUID.randomUUID().toString() + "!" 	|| false
			'dog'       						|| false
			'5'          						|| false
	}

	def "should generate a regex with date [#textToMatch] in YYYY-MM-DD format that should match [#shouldMatch]"() {
		expect:
			shouldMatch == Pattern.compile(regexPatterns.isoDate()).matcher(textToMatch).matches()
		where:
			textToMatch  || shouldMatch
			"2014-03-01" || true
			"1014-03-01" || true
			"1014-3-01"  || false
			"14-03-01"   || false
			"1014-12-01" || true
			"1014-12-31" || true
			"1014-12-1"  || false
			"1014-12-32" || false
			"1014-13-31" || false
			"1014-20-30" || false
			'5'          || false
	}

	def "should generate a regex with datetime [#textToMatch] in YYYY-MM-DDTHH:mm:ss format that should match [#shouldMatch]"() {
		expect:
			shouldMatch == Pattern.compile(regexPatterns.isoDateTime()).matcher(textToMatch).matches()
		where:
			textToMatch           || shouldMatch
			"2014-03-01T12:23:45" || true
			"1014-03-01T23:59:59" || true
			"1014-3-01T01:01:01"  || false
			"1014-03-01T00:00:00" || true
			"1014-03-01T00:00:0"  || false
			"1014-03-01T00:0:01"  || false
			"1014-03-01T0:01:01"  || false
			"1014-03-0100:01:01"  || false
			"14-03-01T12:23:45"   || false
			"1014-12-01T12:23:45" || true
			"1014-12-31T12:23:45" || true
			"1014-12-1T12:23:45"  || false
			"1014-12-32T12:23:45" || false
			"1014-13-31T12:23:45" || false
			"1014-20-30T12:23:45" || false
			"1014-20-30T24:23:45" || false
			"1014-20-30T23:60:45" || false
			"1014-20-30T23:59:60" || false
	}

	def "should generate a regex with time [#textToMatch] in HH:mm:ss format that should match [#shouldMatch]"() {
		expect:
			shouldMatch == Pattern.compile(regexPatterns.isoTime()).matcher(textToMatch).matches()
		where:
			textToMatch || shouldMatch
			"12:23:45"  || true
			"23:59:59"  || true
			"00:00:00"  || true
			"00:00:0"   || false
			"00:0:01"   || false
			"0:01:01"   || false
			"24:23:45"  || false
			"23:60:45"  || false
			"23:59:60"  || false
	}

	def "should generate a regex with iso8601DateTimeWithTimezone [#textToMatch] in YYYY-MM-DDTHH:mm:ss.SSSZZ format that should match [#shouldMatch]"(){
		expect:
			shouldMatch == Pattern.compile(regexPatterns.iso8601DateTimeWithTimeZone()).matcher(textToMatch).matches()
		where:
			textToMatch 										|| shouldMatch
			'2014-03-01T12:23:45Z' 					|| true
			'2014-03-01T12:23:45+01:00' 		|| true
			'2014-03-01T12:23:45.123Z' 			|| true
			'2014-03-01T12:23:45.123+01:00' || true
			'2014-03-01T12:23:45' 					|| false
			'2014-03-01T12:23:45.123' 			|| false
	}

	def "should generate a regex for a not empty or whitespace string [#textToMatch] that should match [#shouldMatch]"(){
		expect:
			shouldMatch == Pattern.compile(regexPatterns.notEmptyOrWhitespace()).matcher(textToMatch).matches()
		where:
			textToMatch || shouldMatch
			'Not Empty' || true
			'' 					|| false
			'    '			|| false
	}

	def "should generate a regex for an enumerated value [#textToMatch] that should match [#shouldMatch]"(){
		expect:
			shouldMatch == RegexPatterns.enumOf('foo','bar').matcher(textToMatch).matches()
		where:
			textToMatch || shouldMatch
			'foo' 			|| true
			'bar' 			|| true
			'baz'				|| false
	}
}
