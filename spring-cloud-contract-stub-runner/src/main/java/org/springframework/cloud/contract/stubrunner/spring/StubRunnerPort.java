package org.springframework.cloud.contract.stubrunner.spring;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotated field with this annotation will have the port of a running stub
 * injected.
 *
 * @author Marcin Grzejszczak
 * @since 2.0.0
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface StubRunnerPort {

	/**
	 * The {@code artifactid} or {@code groupid.artifactid} notation of the started stub
	 */
	String value();

}