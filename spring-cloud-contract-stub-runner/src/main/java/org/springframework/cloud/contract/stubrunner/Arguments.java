/*
 *  Copyright 2013-2016 the original author or authors.
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

package org.springframework.cloud.contract.stubrunner;

/**
 * Arguments passed to the {@link StubRunner} application
 *
 * @see StubRunner
 */
class Arguments {
	final private StubRunnerOptions stubRunnerOptions;
	final private String repositoryPath;
	final private StubConfiguration stub;

	Arguments(StubRunnerOptions stubRunnerOptions) {
		this(stubRunnerOptions, "", null);
	}

	Arguments(StubRunnerOptions stubRunnerOptions, String repositoryPath,
			StubConfiguration stub) {
		this.stubRunnerOptions = stubRunnerOptions;
		this.repositoryPath = repositoryPath == null ? "" : repositoryPath;
		this.stub = stub;
	}

	public StubRunnerOptions getStubRunnerOptions() {
		return this.stubRunnerOptions;
	}

	public String getRepositoryPath() {
		return this.repositoryPath;
	}

	public StubConfiguration getStub() {
		return this.stub;
	}
}