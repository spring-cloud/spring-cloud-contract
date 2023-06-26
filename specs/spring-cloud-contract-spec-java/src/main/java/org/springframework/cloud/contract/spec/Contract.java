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

package org.springframework.cloud.contract.spec;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

import org.springframework.cloud.contract.spec.internal.Input;
import org.springframework.cloud.contract.spec.internal.OutputMessage;
import org.springframework.cloud.contract.spec.internal.Request;
import org.springframework.cloud.contract.spec.internal.Response;

/**
 * The definition of a Contract. Contains helper methods in Groovy left for backward
 * compatibility reasons.
 *
 * @since 1.0.0
 */
public class Contract {

	/**
	 * You can set the level of priority of this contract. If there are two contracts
	 * mapped for example to the same endpoint, then the one with greater priority should
	 * take precedence. A priority of 1 is highest and takes precedence over a priority of
	 * 2.
	 */
	private Integer priority;

	/**
	 * The HTTP request part of the contract.
	 */
	private Request request;

	/**
	 * The HTTP response part of the contract.
	 */
	private Response response;

	/**
	 * The label by which you'll reference the contract on the message consumer side.
	 */
	private String label;

	/**
	 * Description of a contract. May be used in the documentation generation.
	 */
	private String description;

	/**
	 * Name of the generated test / stub. If not provided then the file name will be used.
	 * If you have multiple contracts in a single file and you don't provide this value
	 * then a prefix will be added to the file with the index number while iterating over
	 * the collection of contracts.
	 *
	 * Remember to have a unique name for every single contract. Otherwise you might
	 * generate tests that have two identical methods or you will override the stubs.
	 */
	private String name;

	/**
	 * The input side of a messaging contract.
	 */
	private Input input;

	/**
	 * The output side of a messaging contract.
	 */
	private OutputMessage outputMessage;

	/**
	 * Whether the contract should be ignored or not.
	 */
	private boolean ignored;

	/**
	 * Whether the contract is in progress. It's not ignored, but the feature is not yet
	 * finished. Used together with the {@code generateStubs} option.
	 */
	private boolean inProgress;

	/**
	 * Mapping of metadata. Can be used for external integrations.
	 */
	private Map<String, Object> metadata = new HashMap<>();

	public Contract() {

	}

	/**
	 * You can set the level of priority of this contract. If there are two contracts
	 * mapped for example to the same endpoint, then the one with greater priority should
	 * take precedence. A priority of 1 is highest and takes precedence over a priority of
	 * 2.
	 * @param priority the higher the value the lower the priority
	 */
	public void priority(int priority) {
		this.priority = priority;
	}

	/**
	 * Name of the generated test / stub. If not provided then the file name will be used.
	 * If you have multiple contracts in a single file and you don't provide this value
	 * then a prefix will be added to the file with the index number while iterating over
	 * the collection of contracts.
	 *
	 * Remember to have a unique name for every single contract. Otherwise you might
	 * generate tests that have two identical methods or you will override the stubs.
	 * @param name name of the contract
	 */
	public void name(String name) {
		this.name = name;
	}

	/**
	 * Label used by the messaging contracts to trigger a message on the consumer side.
	 * @param label - name of the label of a messaging contract to trigger
	 */
	public void label(String label) {
		this.label = label;
	}

	/**
	 * Description text. Might be used to describe the usage scenario.
	 * @param description - value of the description
	 */
	public void description(String description) {
		this.description = description;
	}

	public static void assertContract(Contract dsl) {
		if (dsl.getRequest() != null) {
			if (dsl.request.getUrl() == null && dsl.request.getUrlPath() == null) {
				throw new IllegalStateException("URL is missing for HTTP contract");
			}
			if (dsl.request.getMethod() == null) {
				throw new IllegalStateException("Method is missing for HTTP contract");
			}
		}
		if (dsl.response != null) {
			if (dsl.response.getStatus() == null) {
				throw new IllegalStateException("Status is missing for HTTP contract");
			}
		}
		// Can't assert messaging part cause Pact doesn't require destinations it seems
	}

	/**
	 * Point of entry to build a contract.
	 * @param consumer function to manipulate the contract
	 * @return manipulated contract
	 */
	public static Contract make(Consumer<Contract> consumer) {
		Contract contract = new Contract();
		consumer.accept(contract);
		return contract;
	}

	/**
	 * Groovy point of entry to build a contract. Left for backward compatibility reasons.
	 * @param closure function to manipulate the contract
	 * @return manipulated contract
	 */
	public static Contract make(@DelegatesTo(Contract.class) Closure closure) {
		Contract dsl = new Contract();
		closure.setDelegate(dsl);
		closure.call();
		Contract.assertContract(dsl);
		return dsl;
	}

	/**
	 * The HTTP request part of the contract.
	 * @param consumer function to manipulate the request
	 */
	public void request(Consumer<Request> consumer) {
		this.request = new Request();
		consumer.accept(this.request);
	}

	/**
	 * The HTTP response part of the contract.
	 * @param consumer function to manipulate the response
	 */
	public void response(Consumer<Response> consumer) {
		this.response = new Response();
		consumer.accept(this.response);
	}

	/**
	 * The input part of the contract.
	 * @param consumer function to manipulate the input
	 */
	public void input(Consumer<Input> consumer) {
		this.input = new Input();
		consumer.accept(this.input);
	}

	/**
	 * The output part of the contract.
	 * @param consumer function to manipulate the output message
	 */
	public void outputMessage(Consumer<OutputMessage> consumer) {
		this.outputMessage = new OutputMessage();
		consumer.accept(this.outputMessage);
	}

	/**
	 * The HTTP request part of the contract.
	 * @param consumer function to manipulate the request
	 */
	public void request(@DelegatesTo(Request.class) Closure consumer) {
		this.request = new Request();
		consumer.setDelegate(this.request);
		consumer.call();
	}

	/**
	 * The HTTP response part of the contract.
	 * @param consumer function to manipulate the response
	 */
	public void response(@DelegatesTo(Response.class) Closure consumer) {
		this.response = new Response();
		consumer.setDelegate(this.response);
		consumer.call();
	}

	/**
	 * The input part of the contract.
	 * @param consumer function to manipulate the input
	 */
	public void input(@DelegatesTo(Input.class) Closure consumer) {
		this.input = new Input();
		consumer.setDelegate(this.input);
		consumer.call();
	}

	/**
	 * The output part of the contract.
	 * @param consumer function to manipulate the output message
	 */
	public void outputMessage(@DelegatesTo(OutputMessage.class) Closure consumer) {
		this.outputMessage = new OutputMessage();
		consumer.setDelegate(this.outputMessage);
		consumer.call();
	}

	/**
	 * Appends all entries to the existing metadata mapping.
	 * @param map metadata to set
	 */
	public void metadata(Map<String, Object> map) {
		this.metadata.putAll(map);
	}

	/**
	 * Whether the contract should be ignored or not.
	 */
	public void ignored() {
		this.ignored = true;
	}

	/**
	 * Whether the contract is in progress or not.
	 */
	public void inProgress() {
		this.inProgress = true;
	}

	public boolean isInProgress() {
		return this.inProgress;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	public Response getResponse() {
		return response;
	}

	public void setResponse(Response response) {
		this.response = response;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Input getInput() {
		return input;
	}

	public void setInput(Input input) {
		this.input = input;
	}

	public OutputMessage getOutputMessage() {
		return outputMessage;
	}

	public void setOutputMessage(OutputMessage outputMessage) {
		this.outputMessage = outputMessage;
	}

	public boolean getIgnored() {
		return ignored;
	}

	public boolean isIgnored() {
		return ignored;
	}

	public void setIgnored(boolean ignored) {
		this.ignored = ignored;
	}

	public boolean getInProgress() {
		return this.inProgress;
	}

	public void setInProgress(boolean inProgress) {
		this.inProgress = inProgress;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Contract contract = (Contract) o;
		return ignored == contract.ignored && Objects.equals(priority, contract.priority)
				&& Objects.equals(request, contract.request) && Objects.equals(response, contract.response)
				&& Objects.equals(label, contract.label) && Objects.equals(description, contract.description)
				&& Objects.equals(name, contract.name) && Objects.equals(input, contract.input)
				&& Objects.equals(metadata, contract.metadata) && Objects.equals(outputMessage, contract.outputMessage);
	}

	@Override
	public int hashCode() {
		return Objects.hash(priority, request, response, label, description, name, input, outputMessage, metadata,
				ignored);
	}

	@Override
	public String toString() {
		return "Contract{" + "\npriority=" + priority + ", \n\trequest=" + request + ", \n\tresponse=" + response
				+ ", \n\tlabel='" + label + '\'' + ", \n\tdescription='" + description + '\'' + ", \n\tname='" + name
				+ '\'' + ", \n\tinput=" + input + ", \n\toutputMessage=" + outputMessage + ", \n\tignored=" + ignored
				+ '}';
	}

}
