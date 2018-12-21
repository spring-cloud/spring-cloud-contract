package org.springframework.cloud.contract.spec;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines how to store converted contracts to a byte array representation
 * that can be stored to drive
 *
 * @author Marcin Grzejszczak
 * @since 2.1.0
 */
public interface ContractStorer<T> {
	/**
	 * Stores the contracts as a map of filename and String
	 *
	 * @param contracts - to convert
	 * @return mapping of filename to converted byte array representation of the contract
	 */
	default Map<String, byte[]> store(T contracts) {
		Map<String, byte[]> map = new HashMap<>();
		map.put(String.valueOf(Math.abs(hashCode())), contracts.toString().getBytes());
		return map;
	}
}
