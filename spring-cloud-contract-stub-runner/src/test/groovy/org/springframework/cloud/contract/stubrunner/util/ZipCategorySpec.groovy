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

package org.springframework.cloud.contract.stubrunner.util

import groovy.util.logging.Slf4j
import spock.lang.Specification

class ZipCategorySpec extends Specification {

	def 'should unzip a file to the specified location'() {
		given:
		File zipFile = new File(ZipCategorySpec.classLoader.getResource('file.zip').toURI())
		File tempDir = File.createTempDir()
		tempDir.deleteOnExit()
		when:
		use(ZipCategory) {
			zipFile.unzipTo(tempDir)
		}
		then:
		tempDir.listFiles().find {
			it.name == 'file.txt'
		}?.text?.trim() == 'test'
	}

}
