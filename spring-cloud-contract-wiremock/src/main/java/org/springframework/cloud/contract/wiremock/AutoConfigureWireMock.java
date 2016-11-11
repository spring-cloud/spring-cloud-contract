/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.test.autoconfigure.properties.PropertyMapping;
import org.springframework.context.annotation.Import;

/**
 * Annotation for test classes that want to start a WireMock server as part of the Spring
 * Application Context. The port, https port and stub locations (if any) can all be
 * controlled directly here. For more fine-grained control of the server instance add a
 * bean of type {@link com.github.tomakehurst.wiremock.core.Options} to the application context.
 * 
 * @author Dave Syer
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(WireMockConfiguration.class)
@PropertyMapping("wiremock.server")
public @interface AutoConfigureWireMock {

	int port() default 8080;

	int httpsPort() default -1;

	String[] stubs() default {""};

	String[] files() default {""};

}
