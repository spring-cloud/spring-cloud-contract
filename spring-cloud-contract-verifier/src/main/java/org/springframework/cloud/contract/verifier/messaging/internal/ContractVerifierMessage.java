/*
 * Copyright 2012-2015 the original author or authors.
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

package org.springframework.cloud.contract.verifier.messaging.internal;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Yet another message abstraction. Provides generated tests with a layer that is
 * independent of the message provider.
 * 
 * @author Dave Syer
 *
 */
public class ContractVerifierMessage {

	private Object payload;

	private Map<String, Object> headers = new LinkedHashMap<>();

	public ContractVerifierMessage() {
	}

	public ContractVerifierMessage(Object payload, Map<String, Object> headers) {
		this.payload = payload;
		if (headers != null) {
			this.headers.putAll(headers);
		}
	}

	public Object getPayload() {
		return this.payload;
	}

	public void setPayload(Object payload) {
		this.payload = payload;
	}

	public Map<String, Object> getHeaders() {
		return this.headers;
	}
	
	public Object getHeader(String name) {
		return this.headers.get(name);
	}

	public void setHeaders(Map<String, Object> headers) {
		this.headers = headers;
	}

}
