/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.wiremock;

import java.io.IOException;
import java.nio.channels.ByteChannel;

import javax.servlet.http.HttpServletResponse;

import com.github.tomakehurst.wiremock.core.FaultInjector;
import com.google.common.base.Charsets;

import org.eclipse.jetty.io.ChannelEndPoint;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.BufferUtil;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.jetty9.JettyUtils.unwrapResponse;

/**
 * @author Dave Syer
 *
 */
public class JettyFaultInjector implements FaultInjector {

	private static final byte[] GARBAGE = "lskdu018973t09sylgasjkfg1][]'./.sdlv"
			.getBytes(Charsets.UTF_8);

	private final Response response;
	private final ByteChannel socket;

	public JettyFaultInjector(HttpServletResponse response) {
		this.response = unwrapResponse(response);
		this.socket = socket();
	}

	@Override
	public void emptyResponseAndCloseConnection() {
		try {
			this.socket.close();
		}
		catch (IOException e) {
			throwUnchecked(e);
		}
	}

	@Override
	public void malformedResponseChunk() {
		try {
			this.response.setStatus(200);
			this.response.flushBuffer();
			this.socket.write(BufferUtil.toBuffer(GARBAGE));
			this.socket.close();
		}
		catch (IOException e) {
			throwUnchecked(e);
		}

	}

	@Override
	public void randomDataAndCloseConnection() {
		try {
			this.socket.write(BufferUtil.toBuffer(GARBAGE));
			this.socket.close();
		}
		catch (IOException e) {
			throwUnchecked(e);
		}
	}

	private ByteChannel socket() {
		HttpChannel httpChannel = this.response.getHttpOutput().getHttpChannel();
		ChannelEndPoint ep = (ChannelEndPoint) httpChannel.getEndPoint();
		return ep.getChannel();
	}

}
