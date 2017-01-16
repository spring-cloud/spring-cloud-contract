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

package org.springframework.cloud.contract.stubrunner;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tries to execute a closure with an available port from the given range
 */
class AvailablePortScanner {

	private static final Logger log = LoggerFactory.getLogger(AvailablePortScanner.class);

	private static final int MAX_RETRY_COUNT = 1000;

	private final int minPortNumber;
	private final int maxPortNumber;
	private final int maxRetryCount;

	AvailablePortScanner(int minPortNumber, int maxPortNumber) {
		this(minPortNumber, maxPortNumber, MAX_RETRY_COUNT);
	}

	AvailablePortScanner(int minPortNumber, int maxPortNumber, int maxRetryCount) {
		checkPortRanges(minPortNumber, maxPortNumber);
		this.minPortNumber = minPortNumber;
		this.maxPortNumber = maxPortNumber;
		this.maxRetryCount = maxRetryCount;
	}

	private void checkPortRanges(int minPortNumber, int maxPortNumber) {
		if (minPortNumber > maxPortNumber) {
			throw new InvalidPortRange(minPortNumber, maxPortNumber);
		}
	}

	public <T> T tryToExecuteWithFreePort(PortCallback<T> closure) {
		for (int i = 0; i < this.maxRetryCount; i++) {
			try {
				int numberOfPortsToBind = this.maxPortNumber - this.minPortNumber + 1;
				int portToScan = new Random().nextInt(numberOfPortsToBind)
						+ this.minPortNumber;
				checkIfPortIsAvailable(portToScan);
				return executeLogicForAvailablePort(portToScan, closure);
			}
			catch (IOException exception) {
				if (log.isDebugEnabled()) {
					log.debug("Failed to execute callback (try: " + i + "/" + this.maxRetryCount
							+ ")", exception);
				}
			}
		}
		throw new NoPortAvailableException(this.minPortNumber, this.maxPortNumber);
	}

	private <T> T executeLogicForAvailablePort(int portToScan, PortCallback<T> closure) throws IOException {
		if (log.isDebugEnabled()) {
			log.debug("Trying to execute closure with port [" + portToScan + "]");
		}
		return closure.call(portToScan);
	}

	private void checkIfPortIsAvailable(int portToScan) throws IOException {
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(portToScan);
		}
		finally {
			if (socket != null) {
				socket.close();
			}
		}
	}

	@SuppressWarnings("serial")
	static class NoPortAvailableException extends RuntimeException {
		NoPortAvailableException(int lowerBound, int upperBound) {
			super("Could not find available port in range " + lowerBound + ":" + upperBound);
		}
	}

	@SuppressWarnings("serial")
	static class InvalidPortRange extends RuntimeException {
		InvalidPortRange(int lowerBound, int upperBound) {
			super("Invalid bounds exceptions, min port [" + lowerBound
					+ "] is greater to max port [" + upperBound + "]");
		}
	}

	public interface PortCallback<T> {
		T call(int port) throws IOException;
	}
}
