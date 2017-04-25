package org.springframework.cloud.contract.verifier.util

import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 */
class NamesUtilTestSpec extends Specification {

	def "should return the whole string before the last one"() {
		given:
			String string = "a.b.c.d.e"
		expect:
			"a.b.c.d" == NamesUtil.beforeLast(string, ".")
	}

	def "should return empty string when no token was found for before last"() {
		given:
			String string = "a.b.c.d.e"
		expect:
			"" == NamesUtil.beforeLast(string, "/")
	}

	def "should return first token after the last one"() {
		given:
			String string = "a.b.c.d.e"
		expect:
			"e" == NamesUtil.afterLast(string, ".")
	}

	def "should return the input string when no token was found for after last"() {
		given:
			String string = "a.b.c.d.e"
		expect:
			string == NamesUtil.afterLast(string, "/")
	}

	def "should return first token after the last dot"() {
		given:
			String string = "a.b.c.d.e"
		expect:
			"e" == NamesUtil.afterLastDot(string)
	}

	def "should return the input string when no token was found for after last dot"() {
		given:
			String string = "abcde"
		expect:
			string == NamesUtil.afterLastDot(string)
	}

	def "should return camel case version of a string"() {
		given:
			String string = "BlaBlaBla"
		expect:
			"blaBlaBla" == NamesUtil.camelCase(string)
	}

	def "should return capitalized version of a string"() {
		given:
			String string = "blaBlaBla"
		expect:
			"BlaBlaBla" == NamesUtil.capitalize(string)
	}

	def "should return all text to last dot"() {
		given:
			String string = "a.b.c.d.e"
		expect:
			"a.b.c.d" == NamesUtil.toLastDot(string)
	}

	def "should return the input string when no token was found for to last dot"() {
		given:
			String string = "abcde"
		expect:
			string == NamesUtil.toLastDot(string)
	}

	def "should convert a package notation to directory"() {
		given:
			String string = "a.b.c.d.e"
		expect:
			"a/b/c/d/e".replace("/", File.separator) == NamesUtil.packageToDirectory(string)
	}

	def "should convert a directory notation to package"() {
		given:
			String string = "a/b/c/d/e".replace("/", File.separator)
		expect:
			"a.b.c.d.e" == NamesUtil.directoryToPackage(string)
	}

	def "should convert all illegal package chars to legal ones"() {
		given:
			String string = "a-b c.1.0.x"
		expect:
			"a_b_c_1_0_x" == NamesUtil.convertIllegalPackageChars(string)
	}
}
