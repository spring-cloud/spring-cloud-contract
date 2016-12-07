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

package org.springframework.cloud.contract.stubrunner.provider.moco

import com.github.dreamhead.moco.bootstrap.arg.HttpArgs
import com.github.dreamhead.moco.runner.JsonRunner
import org.springframework.cloud.contract.stubrunner.HttpServerStub
import org.springframework.util.SocketUtils

class MocoHttpServerStub implements HttpServerStub {

	private boolean started
	private JsonRunner runner
	private int port

	@Override
	int port() {
		if (!isRunning()) {
			return -1
		}
		return port
	}

	@Override
	boolean isRunning() {
		return started
	}

	@Override
	HttpServerStub start() {
		return start(SocketUtils.findAvailableTcpPort())
	}

	@Override
	HttpServerStub start(int port) {
		this.port = port
		return this
	}

	@Override
	HttpServerStub stop() {
		if (!isRunning()) {
			return this
		}
		this.runner.stop()
		return this
	}

	@Override
	HttpServerStub registerMappings(Collection<File> stubFiles) {
		List<InputStream> streams = stubFiles.collect { it.newInputStream() }
		this.runner = JsonRunner.newJsonRunnerWithStreams(streams,
				HttpArgs.httpArgs().withPort(this.port).build())
		this.runner.run()
		this.started = true
		return this
	}

	@Override
	boolean isAccepted(File file) {
		return file.name.endsWith(".json")
	}
}
