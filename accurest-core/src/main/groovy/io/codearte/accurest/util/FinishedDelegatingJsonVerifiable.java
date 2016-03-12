package io.codearte.accurest.util;

import com.toomuchcoding.jsonassert.JsonVerifiable;

/**
 * @author Marcin Grzejszczak
 */
class FinishedDelegatingJsonVerifiable extends DelegatingJsonVerifiable {

	FinishedDelegatingJsonVerifiable(JsonVerifiable delegate,
			StringBuffer methodsBuffer) {
		super(delegate, methodsBuffer);
	}

	FinishedDelegatingJsonVerifiable(JsonVerifiable delegate) {
		super(delegate, new StringBuffer());
	}

}
