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

package org.springframework.cloud.contract.verifier.file


import groovy.transform.CompileStatic

/**
 * Scans the provided file path for the DSLs. There's a possibility to provide
 * inclusion and exclusion filters.
 *
 * @author Jakub Kubrynski, codearte.io
 *
 * @since 2.1.0
 */
@CompileStatic
class ContractFileScannerBuilder {

	private File baseDir
	private Set<String> excluded
	private Set<String> ignored
	private Set<String> included = []
	private String includeMatcher = ""

	ContractFileScannerBuilder baseDir(File baseDir) {
		this.baseDir = baseDir
		return this
	}

	ContractFileScannerBuilder excluded(Set<String> excluded) {
		this.excluded = excluded
		return this
	}

	ContractFileScannerBuilder ignored(Set<String> ignored) {
		this.ignored = ignored
		return this
	}

	ContractFileScannerBuilder included(Set<String> included) {
		this.included = included
		return this
	}

	ContractFileScannerBuilder includeMatcher(String includeMatcher) {
		this.includeMatcher = includeMatcher
		return this
	}

	ContractFileScanner build() {
		return new ContractFileScanner(this.baseDir,
				this.excluded,
				this.ignored,
				this.included,
				this.includeMatcher)
	}
}

