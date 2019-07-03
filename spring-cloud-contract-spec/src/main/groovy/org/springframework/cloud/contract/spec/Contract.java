package org.springframework.cloud.contract.spec;

import org.springframework.cloud.contract.spec.internal.Input;
import org.springframework.cloud.contract.spec.internal.OutputMessage;
import org.springframework.cloud.contract.spec.internal.Request;
import org.springframework.cloud.contract.spec.internal.Response;

/**
 * The point of entry to the DSL
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
	 * The HTTP request part of the contract
	 */
	private Request request;

	/**
	 * The HTTP response part of the contract
	 */
	private Response response;

	/**
	 * The label by which you'll reference the contract on the message consumer side
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

	public Contract() {
	}

	/**
	 * You can set the level of priority of this contract. If there are two contracts
	 * mapped for example to the same endpoint, then the one with greater priority should
	 * take precedence. A priority of 1 is highest and takes precedence over a priority of
	 * 2.
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
	 */
	public void name(String name) {
		this.name = name;
	}

	/**
	 * Label used by the messaging contracts to trigger a message on the consumer side
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
	 * Whether the contract should be ignored or not.
	 */
	public void ignored() {
		this.ignored = true;
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

}
