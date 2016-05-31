package io.codearte.accurest.util;

import java.util.LinkedList;

import com.toomuchcoding.jsonassert.JsonVerifiable;

/**
 * @author Marcin Grzejszczak
 */
class FinishedDelegatingJsonVerifiable extends DelegatingJsonVerifiable {

	FinishedDelegatingJsonVerifiable(JsonVerifiable delegate,
									 LinkedList<String> methodsBuffer) {
		super(delegate, methodsBuffer);
	}

}
