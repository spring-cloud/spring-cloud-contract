/*
 *  Copyright 2013-2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.verifier.builder

import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties

/**
 * A {@link JUnitMethodBodyBuilder} implementation that uses Rest Assured in explicit mode
 *
 * @author Marcin Grzejszczak
 *
 * @since 1.0.3
 */
@TypeChecked
@PackageScope
class ExplicitJUnitMethodBodyBuilder extends RestAssuredJUnitMethodBodyBuilder {

	ExplicitJUnitMethodBodyBuilder(Contract stubDefinition, ContractVerifierConfigProperties configProperties, String methodName) {
		super(stubDefinition, configProperties, methodName)
	}

	@Override
	protected String returnedResponseType() {
		return "Response"
	}

	@Override
	protected String returnedRequestType() {
		return "RequestSpecification"
	}

}
