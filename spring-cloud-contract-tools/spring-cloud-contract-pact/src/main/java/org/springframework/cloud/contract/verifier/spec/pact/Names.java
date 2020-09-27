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

package org.springframework.cloud.contract.verifier.spec.pact;

/**
 * @author Marcin Grzejszczak
 * @since
 */
class Names {

	private final String consumer;

	private final String producer;

	private final String test;

	Names(String[] strings) {
		this.consumer = strings[0];
		this.producer = strings[1];
		this.test = strings.length >= 2 ? strings[2] : "";
	}

	@Override
	public String toString() {
		return this.consumer + "_" + this.producer;
	}

	String getConsumer() {
		return consumer;
	}

	String getProducer() {
		return producer;
	}

}
