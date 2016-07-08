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

package org.springframework.cloud.contract.verifier.messaging.camel;

import org.springframework.cloud.contract.verifier.messaging.ContractVerifierMessage;
import org.apache.camel.Message;

import java.util.Map;

/**
 * @author Marcin Grzejszczak
 */
public class CamelMessage<T> implements ContractVerifierMessage<T, Message> {

	private final Message delegate;

	public CamelMessage(Message delegate) {
		this.delegate = delegate;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T getPayload() {
		return (T) delegate.getBody();
	}

	@Override
	public Map<String, Object> getHeaders() {
		return delegate.getHeaders();
	}

	@Override
	public Object getHeader(String key) {
		return getHeaders().get(key);
	}

	@Override
	public Message convert() {
		return delegate;
	}

}
