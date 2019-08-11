/*
 * Copyright 2013-2019 the original author or authors.
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

package org.springframework.cloud.contract.stubrunner

import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 */
class ContractDownloaderSpec extends Specification {

	StubDownloader stubDownloader = Stub()
	StubConfiguration stubConfiguration = new StubConfiguration('')

	File file = new File(File.separator + ['some', 'path', 'to', 'somewhere'].join(File.separator))

	def 'should set inclusion pattern on config when path pattern was explicitly provided with a separator at the beginning'() {
		given:
			String contractPath = File.separator + ['a', 'b', 'c', 'd'].join(File.separator)
			ContractDownloader contractDownloader = new ContractDownloader(stubDownloader,
					stubConfiguration, contractPath, '', '', '')
		when:
			ContractDownloader.InclusionProperties inclusionProperties = contractDownloader.createNewInclusionProperties(file)
		then:
			inclusionProperties.includedContracts.startsWith('^')
			inclusionProperties.includedContracts.endsWith('$')
			inclusionProperties.includedContracts.contains(fileSeparated('/some/path/to/somewhere(/)?.*/a/b/c/d/.*'))
			inclusionProperties.includedRootFolderAntPattern == "**/a/b/c/d/**/"
	}

	def 'should set inclusion pattern on config when path pattern was explicitly provided without a separator at the beginning'() {
		given:
			String contractPath = ['a', 'b', 'c', 'd'].join(File.separator)
			ContractDownloader contractDownloader = new ContractDownloader(stubDownloader,
					stubConfiguration, contractPath, '', '', '')
		when:
			ContractDownloader.InclusionProperties inclusionProperties =
				contractDownloader.createNewInclusionProperties(file)
		then:
			inclusionProperties.includedContracts.startsWith('^')
			inclusionProperties.includedContracts.endsWith('$')
			inclusionProperties.includedContracts.contains(fileSeparated('/some/path/to/somewhere(/)?.*/a/b/c/d/.*'))
			inclusionProperties.includedRootFolderAntPattern == "**/a/b/c/d/**/"
	}

	private static String fileSeparated(String string) {
		return string.replace('/', File.separator).replace("\\", "\\\\")
	}
}
