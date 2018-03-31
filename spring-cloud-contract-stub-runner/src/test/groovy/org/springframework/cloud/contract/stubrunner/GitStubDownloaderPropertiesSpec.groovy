/*
 *  Copyright 2013-2018 the original author or authors.
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

package org.springframework.cloud.contract.stubrunner

import spock.lang.Specification

import org.springframework.core.io.AbstractResource
import org.springframework.core.io.Resource

/**
 * @author Marcin Grzejszczak
 */
class GitStubDownloaderPropertiesSpec extends Specification {

	def "should parse only the URL after protocol if it doesn't start with git"() {
		given:
			Resource resource = resource("git://https://foo.com")
		when:
			GitStubDownloaderProperties props = new GitStubDownloaderProperties(resource, new StubRunnerOptionsBuilder().build())
		then:
			props.url == URI.create("https://foo.com")
	}

	def "should return the whole address if it starts with git@ but doesn't finish with .git"() {
		given:
			Resource resource = resource("git://git@foo.com/foo")
		when:
			GitStubDownloaderProperties props = new GitStubDownloaderProperties(resource, new StubRunnerOptionsBuilder().build())
		then:
			props.url == URI.create("git:git@foo.com/foo")
	}

	Resource resource(String uri) {
		return new AbstractResource() {
			@Override
			String getDescription() {
				return null
			}

			@Override
			InputStream getInputStream() throws IOException {
				return null
			}

			@Override
			URI getURI() throws IOException {
				return URI.create(uri)
			}
		}
	}
}
