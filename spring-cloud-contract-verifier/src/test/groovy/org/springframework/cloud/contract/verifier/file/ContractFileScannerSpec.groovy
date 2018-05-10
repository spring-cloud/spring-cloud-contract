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

package org.springframework.cloud.contract.verifier.file

import wiremock.com.google.common.collect.ListMultimap
import spock.lang.Specification

import java.nio.file.Path

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.ContractConverter

/**
 * @author Jakub Kubrynski, codearte.io
 */
class ContractFileScannerSpec extends Specification {

	def "should find contract files"() {
		given:
			File baseDir = new File(this.getClass().getResource("/directory/with/stubs").toURI())
			Set<String> excluded = ["package/**"] as Set
			Set<String> ignored = ["other/different/**"] as Set
			ContractFileScanner scanner = new ContractFileScanner(baseDir, excluded, ignored, [] as Set)
		when:
			ListMultimap<Path, ContractMetadata> result = scanner.findContracts()
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
			ListMultimap<Path, ContractMetadata> result = scanner.findContracts()
		then:
			result.entries().size() == 2
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
			ListMultimap<Path, ContractMetadata> contracts = scanner.findContracts()
		then:
			contracts.values().size() == 3
			contracts.values().find { it.path.fileName.toString().startsWith('01') }.groupSize == 3
			contracts.values().find { it.path.fileName.toString().startsWith('01') }.order == 0
			contracts.values().find { it.path.fileName.toString().startsWith('02') }.order == 1
			contracts.values().find { it.path.fileName.toString().startsWith('03') }.order == 2
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
			ListMultimap<Path, ContractMetadata> result = scanner.findContracts()
		then:
			result.keySet().size() == 1
			result.entries().every { it.value.convertedContract }
			result.entries().find { it.value.convertedContract.any { it.request.method.clientValue == "PUT" } }
	}

    def "should find contracts for include pattern"() {
        given:
            File baseDir = new File(this.getClass().getResource("/directory/with/common-messaging").toURI())
            Set<String> included = ["social-service/**","**/coupon-collected/**/*V1*"] as Set
            ContractFileScanner scanner = new ContractFileScanner(baseDir, [] as Set, [] as Set, included)
        when:
            ListMultimap<Path, ContractMetadata> result = scanner.findContracts()
        then:
            result.keySet().size() == 3
            result.values().find { (it.path.fileName.toString() == 'couponCollectedEventV1.groovy') }.groupSize==2
            result.values().find { (it.convertedContract.first().label == 'couponCollectedV1') }
            result.values().findAll { (it.path.fileName.toString() == 'couponCollectedEventV2.groovy') }.isEmpty()
            result.values().find { (it.path.fileName.toString() == 'shouldUpdateUserInfo.groovy') }.groupSize==1
            result.values().find { (it.path.fileName.toString() == 'shouldReturnEmptyFriendsWhenGetFriends.groovy') }.groupSize==1
            result.get(baseDir.toPath().resolve("coupon-sent")).size() == 0
            result.get(baseDir.toPath().resolve("reward-rules")).size() == 0
    }
}
