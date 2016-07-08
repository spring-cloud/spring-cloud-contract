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

package org.springframework.cloud.contract.stubrunner

interface StubDownloader {

	/**
	 * Returns a mapping of updated StubConfiguration (it will contain the resolved version) and the location of the downloaded JAR.
	 * If there was no artifact this method will return {@code null}.
	 */
	Map.Entry<StubConfiguration,File> downloadAndUnpackStubJar(StubRunnerOptions options, StubConfiguration stubConfiguration)


}