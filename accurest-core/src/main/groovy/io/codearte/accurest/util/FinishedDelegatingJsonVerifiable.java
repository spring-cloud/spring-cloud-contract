package io.codearte.accurest.util;

import com.toomuchcoding.jsonassert.JsonVerifiable;

import java.util.LinkedList;

/**
 * @author Marcin Grzejszczak
 */
class FinishedDelegatingJsonVerifiable extends DelegatingJsonVerifiable {

	FinishedDelegatingJsonVerifiable(JsonVerifiable delegate,
									 LinkedList<String> methodsBuffer) {
		super(delegate, methodsBuffer);
	}

	FinishedDelegatingJsonVerifiable(JsonVerifiable delegate) {
		super(delegate, new LinkedList<String>());
	}

}
