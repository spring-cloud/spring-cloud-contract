/*
 * Copyright 2012-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.wiremock;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.test.autoconfigure.properties.PropertyMapping;
import org.springframework.boot.test.autoconfigure.properties.SkipPropertyMapping;
import org.springframework.context.annotation.Import;

/**
 * Annotation for test classes that want to start a WireMock server as part of the Spring
 * Application Context. The port, https port and stub locations (if any) can all be
 * controlled directly here. For more fine-grained control of the server instance add a
 * bean of type {@link com.github.tomakehurst.wiremock.core.Options} to the application
 * context.
 *
 * @author Dave Syer
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(WireMockConfiguration.class)
@PropertyMapping(value = "wiremock.server", skip = SkipPropertyMapping.ON_DEFAULT_VALUE)
@AutoConfigureHttpClient
@Inherited
public @interface AutoConfigureWireMock {

	/**
	 * Configures WireMock instance to listen on specified port.
	 * <p>
	 * Set this value to 0 for WireMock to listen to a random port.
	 * </p>
	 * @return port to which WireMock instance should listen to
	 */
	int port() default 8080;

	/**
	 * If specified, configures WireMock instance to enable <em>HTTPS</em> on specified
	 * port.
	 * <p>
	 * Set this value to 0 for WireMock to listen to a random port.
	 * </p>
	 * @return port to which WireMock instance should listen to
	 */
	int httpsPort() default -1;

	/**
	 * The resource locations to use for loading WireMock mappings.
	 * <p>
	 * When none specified, <em>src/test/resources/mappings</em> is used as default
	 * location.
	 * </p>
	 * <p>
	 * To customize the location, this attribute must be set to the directory where
	 * mappings are stored.
	 * </p>
	 * @return locations to read WireMock mappings from
	 */
	String[] stubs() default { "" };

	/**
	 * The resource locations to use for loading WireMock response bodies.
	 * <p>
	 * When none specified, <em>src/test/resources/__files</em> is used as default.
	 * </p>
	 * <p>
	 * To customize the location, this attribute must be set to the parent directory of
	 * <strong>__files</strong> directory.
	 * </p>
	 * @return locations to read WireMock response bodies from
	 */
	String[] files() default { "" };

}
