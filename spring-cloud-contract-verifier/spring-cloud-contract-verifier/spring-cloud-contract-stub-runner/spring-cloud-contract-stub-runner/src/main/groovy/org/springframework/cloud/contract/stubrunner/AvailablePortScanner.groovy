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

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.util.logging.Slf4j

/**
 * Tries to execute a closure with an available port from the given range
 */
@CompileStatic
@Slf4j
@PackageScope
class AvailablePortScanner {

	private static final int MAX_RETRY_COUNT = 1000

	private final int minPortNumber
	private final int maxPortNumber
	private final int maxRetryCount

	AvailablePortScanner(int minPortNumber, int maxPortNumber, int maxRetryCount = MAX_RETRY_COUNT) {
		checkPortRanges(minPortNumber, maxPortNumber)
		this.minPortNumber = minPortNumber
		this.maxPortNumber = maxPortNumber
		this.maxRetryCount = maxRetryCount
	}

	private void checkPortRanges(int minPortNumber, int maxPortNumber) {
		if (minPortNumber > maxPortNumber) {
			throw new InvalidPortRange(minPortNumber, maxPortNumber)
		}
	}

	public <T> T tryToExecuteWithFreePort(Closure<T> closure) {
		for (i in (1..maxRetryCount)) {
			try {
				int numberOfPortsToBind = maxPortNumber - minPortNumber + 1
				int portToScan = new Random().nextInt(numberOfPortsToBind) + minPortNumber
				checkIfPortIsAvailable(portToScan)
				return executeLogicForAvailablePort(portToScan, closure)
			} catch (BindException exception) {
				log.debug("Failed to execute closure (try: $i/$maxRetryCount)", exception)
			}
		}
		throw new NoPortAvailableException(minPortNumber, maxPortNumber)
	}

	private <T> T executeLogicForAvailablePort(int portToScan, Closure<T> closure) {
		log.debug("Trying to execute closure with port [$portToScan]")
		return closure(portToScan)
	}

	private void checkIfPortIsAvailable(int portToScan) {
		ServerSocket socket = null
		try {
			socket = new ServerSocket(portToScan)
		} finally {
			socket?.close()
		}
	}

	static class NoPortAvailableException extends RuntimeException {
		protected NoPortAvailableException(int lowerBound, int upperBound) {
			super("Could not find available port in range $lowerBound:$upperBound")
		}
	}

	static class InvalidPortRange extends RuntimeException {
		protected InvalidPortRange(int lowerBound, int upperBound) {
			super("Invalid bounds exceptions, min port [$lowerBound] is greater to max port [$upperBound]")
		}
	}
}
