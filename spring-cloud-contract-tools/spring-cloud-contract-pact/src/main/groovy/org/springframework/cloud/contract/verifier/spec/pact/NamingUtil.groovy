package org.springframework.cloud.contract.verifier.spec.pact

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.springframework.cloud.contract.spec.Contract

/**
 * @author Marcin Grzejszczak
 * @since
 */
@PackageScope
@CompileStatic
final class NamingUtil {

	// consumer___producer___testname
	private static final String SEPARATOR = "___"

	protected static Names name(Contract contract) {
		String contractName = contract.name
		if (!contractName || !contractName.contains(SEPARATOR)) {
			return new Names(["Consumer", "Provider" , ""] as String[])
		}
		return new Names(contractName.split(SEPARATOR))
	}
}

@PackageScope
@CompileStatic
class Names {
	final String consumer
	final String producer
	final String test

	Names(String[] strings) {
		this.consumer = strings[0]
		this.producer = strings[1]
		this.test = strings.length >= 2 ? strings[2] : ""
	}


	@Override
	String toString() {
		return this.consumer + "_" + this.producer
	}
}
