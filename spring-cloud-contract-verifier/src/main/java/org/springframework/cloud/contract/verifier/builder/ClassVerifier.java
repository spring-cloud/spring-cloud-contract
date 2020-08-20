package org.springframework.cloud.contract.verifier.builder;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Used to return the {@link Class} against which the type of the element should be
 * verified using <code>instanceof</code> in generated response assertions.
 *
 * @author Marcin Grzejszczak
 * @author Olga Maciaszek-Sharma
 * @since 2.1.0
 */
public interface ClassVerifier {

	default Class classToCheck(Object elementFromBody) {
		if (elementFromBody instanceof List) {
			return List.class;
		}
		else if (elementFromBody instanceof Set) {
			return Set.class;
		}
		else if (elementFromBody instanceof Map) {
			return Map.class;
		}
		return elementFromBody.getClass();
	}

}
