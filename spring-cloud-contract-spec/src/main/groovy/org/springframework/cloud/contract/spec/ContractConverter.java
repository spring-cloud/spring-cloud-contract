package org.springframework.cloud.contract.spec;

import java.io.File;

/**
 * Converter to be used to convert FROM {@link File} TO {@link Contract}
 * and from {@link Contract} to {@link T}
 *
 * @param <T> - type to which we want to convert the contract
 *
 * @author Marcin Grzejszczak
 * @since 1.0.3
 */
public interface ContractConverter<T> {

	/**
	 * Should this file be accepted by the converter. Can use the file extension
	 * to check if the conversion is possible.
	 *
	 * @param file - file to be considered for conversion
	 * @return - {@code true} if the given implementation can convert the file
	 */
	boolean isAccepted(File file);

	/**
	 * Converts the given {@link File} to its {@link Contract} representation
	 *
	 * @param file - file to convert
	 * @return - {@link Contract} representation of the file
	 */
	Contract convertFrom(File file);

	/**
	 * Converts the given {@link Contract} to a {@link T} representation
	 *
	 * @param contract - the parsed contract
	 * @return - {@link T} the type to which we do the conversion
	 */
	T convertTo(Contract contract);
}
