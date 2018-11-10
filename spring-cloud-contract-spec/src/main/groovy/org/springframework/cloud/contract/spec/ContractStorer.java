package org.springframework.cloud.contract.spec;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines how to store converted contracts to a String representation
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
	 * @return mapping of filename to converted String representation of the contract
	 */
	default Map<String, String> storeAsString(T contracts) {
		Map<String, String> map = new HashMap<>();
		map.put(String.valueOf(Math.abs(hashCode())), contracts.toString());
		return map;
	}
}
