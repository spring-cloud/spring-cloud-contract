/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.verifier.util

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import org.springframework.util.FileSystemUtils

/**
 * @author Marcin Grzejszczak
 */
class NamesUtilSpec extends Specification {

	@Rule
	TemporaryFolder folder = new TemporaryFolder()

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

	def "should convert a directory notation to package when folder is only a digit"() {
		given:
			String string = "1.0.0"
		expect:
			NamesUtil.directoryToPackage(string) == "_1_0_0"
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
			NamesUtil.convertIllegalMethodNameChars(string) == '10a_b_c_1_0_x_d1174$dd'
	}

	def "should recursively convert the names of folders to package names"() {
		given:
			File tmp = folder.newFolder()
			URL resource = getClass().getResource("/prependFolderName")
			File folder = new File(resource.toURI())
			FileSystemUtils.copyRecursively(folder, tmp)
		when:
			NamesUtil.recrusiveDirectoryToPackage(tmp)
		then:
			new File(tmp, "META-INF/1_0_0_SNAPSHOT").exists() == false
			new File(tmp, "META-INF/2_0_0_SNAPSHOT").exists() == false
			new File(tmp, "META-INF/1_0_0_SNAPSHOT/3_0_0_SNAPSHOT").exists() == false
			new File(tmp, "META-INF/_1_0_0_SNAPSHOT").exists() == true
			new File(tmp, "META-INF/_2_0_0_SNAPSHOT").exists() == true
			new File(tmp, "META-INF/_1_0_0_SNAPSHOT/_3_0_0_SNAPSHOT").exists() == true
			new File(tmp, "META-INF/_1_0_0_SNAPSHOT/normal").exists() == true
			new File(tmp, "META-INF/_1_0_0_SNAPSHOT/_3_0_0_SNAPSHOT/normal").exists() == true
	}

	def "should not throw exception if folder does not exist"() {
		when:
			NamesUtil.recrusiveDirectoryToPackage(new File("I/do/not/exist"))
		then:
			noExceptionThrown()
	}
}
