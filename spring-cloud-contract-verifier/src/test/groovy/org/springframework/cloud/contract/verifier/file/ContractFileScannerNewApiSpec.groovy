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

package org.springframework.cloud.contract.verifier.file


import java.nio.file.Path

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.ContractConverter
import org.springframework.util.FileSystemUtils
import org.springframework.util.MultiValueMap

/**
 * @author Jakub Kubrynski, codearte.io
 */
class ContractFileScannerNewApiSpec extends Specification {

	@Rule
	TemporaryFolder tmp = new TemporaryFolder()
	File tmpFolder

	def setup() {
		tmpFolder = new File(tmp.newFolder(), "contracts")
	}

	def "should find contract files"() {
		given:
			FileSystemUtils.copyRecursively(
					new File(this.getClass().getResource("/directory/with/stubs").toURI()),
					tmpFolder)
		and:
			File baseDir = tmpFolder
			Set<String> excluded = ["package/**"] as Set
			Set<String> ignored = ["other/different/**"] as Set
			ContractFileScanner scanner = new ContractFileScanner(baseDir, excluded, ignored, [] as Set)
		when:
			MultiValueMap<Path, ContractMetadata> result = scanner.findContractsRecursively()
		then:
			result.keySet().size() == 3
			result.get(baseDir.toPath().resolve("different")).size() == 1
			result.get(baseDir.toPath().resolve("other")).size() == 2
		and:
			Collection<ContractMetadata> ignoredSet = result.get(baseDir.toPath().resolve("other").resolve("different"))
			ignoredSet.size() == 1
			ignoredSet.ignored == [true]
	}

	def "should find contract files in strange directories"() {
		given:
			File baseDir = new File(this.getClass().getResource("/strange_[3.3.3]_directory").toURI())
			Set<String> excluded = ["foo/**"] as Set
			Set<String> ignored = ["bar/**"] as Set
			ContractFileScanner scanner = new ContractFileScanner(baseDir, excluded, ignored, [] as Set)
		when:
			MultiValueMap<Path, ContractMetadata> result = scanner.findContractsRecursively()
		then:
			result.entrySet().size() == 2
		and:
			Collection<ContractMetadata> ignoredSet = result.get(baseDir.toPath().resolve("bar"))
			ignoredSet.size() == 1
			ignoredSet.ignored == [true]
	}

	def "should find contracts group in scenario"() {
		given:
			File baseDir = new File(this.getClass().getResource("/directory/with/scenario").toURI())
			ContractFileScanner scanner = new ContractFileScanner(baseDir, [] as Set, [] as Set, [] as Set)
		when:
			MultiValueMap<Path, ContractMetadata> contracts = scanner.findContractsRecursively()
		then:
			contracts.values().size() == 1
			def firstEntry = contracts.values().first()
			firstEntry.size() == 3
			firstEntry.find {
				it.path.fileName.toString().startsWith('01')
			}.groupSize == 3
			firstEntry.find {
				it.path.fileName.toString().startsWith('01')
			}.order == 0
			firstEntry.find {
				it.path.fileName.toString().startsWith('02')
			}.order == 1
			firstEntry.find {
				it.path.fileName.toString().startsWith('03')
			}.order == 2
	}

	def "should find contract files with converters"() {
		given:
			File baseDir = new File(this.getClass().getResource("/directory/with/mixed").toURI())
			ContractFileScanner scanner = new ContractFileScanner(baseDir, null, null, null) {
				@Override
				protected List<ContractConverter> converters() {
					return [new ContractConverter() {
						@Override
						boolean isAccepted(File file) {
							return file.name.endsWith(".json")
						}

						@Override
						Collection<Contract> convertFrom(File file) {
							throw new RuntimeException("boom")
						}

						@Override
						Object convertTo(Collection contract) {
							throw new RuntimeException("boom")
						}
					}]
				}
			}
		when:
			scanner.findContractsRecursively()
		then:
			IllegalStateException e = thrown(IllegalStateException)
			e.cause.message == "boom"
			e.message.matches(".*Failed to convert file .*invalid.json.*")
	}

	def "should prefer custom yaml converter over standard yaml converter"() {
		given:
			File baseDir = new File(this.getClass().getResource("/directory/with/custom/yml").toURI())
			ContractFileScanner scanner = new ContractFileScanner(baseDir, null, null) {
				@Override
				protected List<ContractConverter> converters() {
					return [new ContractConverter() {
						@Override
						boolean isAccepted(File file) {
							if (!file.name.endsWith(".yml") && !file.name.endsWith(".yaml")) {
								return false
							}
							String line
							file.withReader {
								line = it.readLine()
							}
							return line != null && line.startsWith("custom_format: 1.0")
						}

						@Override
						Collection<Contract> convertFrom(File file) {
							return Collections.singleton(Contract.newInstance())
						}

						@Override
						Object convertTo(Collection contract) {
							return new Object()
						}
					}]
				}
			}
		when:
			MultiValueMap<Path, ContractMetadata> result = scanner.findContractsRecursively()
		then:
			result.keySet().size() == 1
			result.entrySet().every { it.value.convertedContract }
	}

	def "should find contracts for include pattern"() {
		given:
			FileSystemUtils.copyRecursively(
					new File(this.getClass().getResource("/directory/with/common-messaging").toURI()),
					tmpFolder)
		and:
			File baseDir = tmpFolder
			Set<String> included = ["social-service/**", "**/coupon-collected/**/*V1*"] as Set
			ContractFileScanner scanner = new ContractFileScanner(baseDir, [] as Set, [] as Set, included)
		when:
			MultiValueMap<Path, ContractMetadata> result = scanner.findContractsRecursively()
		then:
			result.keySet().size() == 3
			result.values().flatten().find {
				(it.path.fileName.toString() == 'couponCollectedEventV1.groovy')
			}.groupSize == 2
			result.values().flatten().find {
				(it.convertedContract.first().label == 'couponCollectedV1')
			}
			result.values().flatten().findAll {
				(it.path.fileName.toString() == 'couponCollectedEventV2.groovy')
			}.isEmpty()
			result.values().flatten().find {
				(it.path.fileName.toString() == 'shouldUpdateUserInfo.groovy')
			}.groupSize == 1
			result.values().flatten().find {
				(it.path.fileName.toString() == 'shouldReturnEmptyFriendsWhenGetFriends.groovy')
			}.groupSize == 1
			result.get(baseDir.toPath().resolve("coupon-sent")) == null
			result.get(baseDir.toPath().resolve("reward-rules")) == null
	}
}
