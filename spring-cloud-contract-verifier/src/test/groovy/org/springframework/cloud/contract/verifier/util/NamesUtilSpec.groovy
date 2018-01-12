package org.springframework.cloud.contract.verifier.util

import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 */
class NamesUtilSpec extends Specification {

	def "should return the whole string before the last one"() {
		given:
			String string = "a.b.c.d.e"
		expect:
			NamesUtil.beforeLast(string, ".") == "a.b.c.d"
	}

	def "should return empty string when no token was found for before last"() {
		given:
			String string = "a.b.c.d.e"
		expect:
			NamesUtil.beforeLast(string, "/") == ""
	}

	def "should return first token after the last one"() {
		given:
			String string = "a.b.c.d.e"
		expect:
			NamesUtil.afterLast(string, ".") == "e"
	}

	def "should return the input string when no token was found for after last"() {
		given:
			String string = "a.b.c.d.e"
		expect:
			NamesUtil.afterLast(string, "/") == string
	}

	def "should return first token after the last dot"() {
		given:
			String string = "a.b.c.d.e"
		expect:
			NamesUtil.afterLastDot(string) == "e"
	}

	def "should return the input string when no token was found for after last dot"() {
		given:
			String string = "abcde"
		expect:
			NamesUtil.afterLastDot(string) == string
	}

	def "should return camel case version of a string"() {
		given:
			String string = "BlaBlaBla"
		expect:
			NamesUtil.camelCase(string) == "blaBlaBla"
	}

	def "should return capitalized version of a string"() {
		given:
			String string = "blaBlaBla"
		expect:
			NamesUtil.capitalize(string) == "BlaBlaBla"
	}

	def "should return all text to last dot"() {
		given:
			String string = "a.b.c.d.e"
		expect:
			NamesUtil.toLastDot(string) == "a.b.c.d"
	}

	def "should return the input string when no token was found for to last dot"() {
		given:
			String string = "abcde"
		expect:
			NamesUtil.toLastDot(string) == string
	}

	def "should convert a package notation to directory"() {
		given:
			String string = "a.b.c.d.e"
		expect:
			NamesUtil.packageToDirectory(string) == "a/b/c/d/e".replace("/", File.separator)
	}

	def "should convert a directory notation to package"() {
		given:
			String string = "a/b/c/d/e".replace("/", File.separator)
		expect:
			NamesUtil.directoryToPackage(string) == "a.b.c.d.e"
	}

	def "should convert a directory notation to package when folder is a digit"() {
		given:
			String string = "a/b/c/1.0.0/e".replace("/", File.separator)
		expect:
			NamesUtil.directoryToPackage(string) == "a.b.c._1_0_0.e"
	}

	def "should convert all illegal package chars to legal ones"() {
		given:
			String string = "a-b c.1.0.x+d1174dd"
		expect:
			NamesUtil.convertIllegalPackageChars(string) == "a_b_c_1_0_x_d1174dd"
	}

	def "should convert all illegal method chars to legal ones"() {
		given:
		String string = '10a-b c.1.0.x+d1174$dd'
		expect:
		NamesUtil.convertIllegalMethodNameChars(string) == '_0a_b_c_1_0_x_d1174$dd'
	}
}
