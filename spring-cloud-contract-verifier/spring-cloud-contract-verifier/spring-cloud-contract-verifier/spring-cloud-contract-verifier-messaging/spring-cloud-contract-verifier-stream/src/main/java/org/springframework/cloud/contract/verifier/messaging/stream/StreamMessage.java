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

package org.springframework.cloud.contract.verifier.messaging.stream;

import org.springframework.cloud.contract.verifier.messaging.ContractVerifierMessage;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

/**
 * @author Marcin Grzejszczak
 */
public class StreamMessage<T> implements ContractVerifierMessage<T, Message<T>> {

	private final Message<T> delegate;

	public StreamMessage(Message<T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public T getPayload() {
		return delegate.getPayload();
	}

	@Override
	public MessageHeaders getHeaders() {
		return delegate.getHeaders();
	}

	@Override
	public Object getHeader(String key) {
		return getHeaders().get(key);
	}

	@Override
	public Message<T> convert() {
		return delegate;
	}

}
