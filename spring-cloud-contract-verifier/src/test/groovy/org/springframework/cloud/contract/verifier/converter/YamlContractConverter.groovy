/*
 *  Copyright 2013-2016 the original author or authors.
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

package org.springframework.cloud.contract.verifier.converter

import groovy.transform.CompileStatic
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.ContractConverter
import org.springframework.cloud.contract.spec.internal.Headers
import org.yaml.snakeyaml.Yaml

/**
 * Simple converter from and to a {@link YamlContract} to a collection of {@link Contract}
 */
@CompileStatic
class YamlContractConverter implements ContractConverter<List<YamlContract>> {

	@Override
	public boolean isAccepted(File file) {
		String name = file.getName()
		return name.endsWith(".yml") || name.endsWith(".yaml")
	}

	@Override
	public Collection<Contract> convertFrom(File file) {
		try {
			YamlContract yamlContract = new Yaml().loadAs(new FileInputStream(file), YamlContract.class)
			return [Contract.make {
				request {
					method(yamlContract?.request?.method)
					url(yamlContract?.request?.url)
					headers {
						yamlContract?.request?.headers?.each { String key, Object value ->
							header(key, value)
						}
					}
					body(yamlContract?.request?.body)
				}
				response {
					status(yamlContract?.response?.status)
					headers {
						yamlContract?.response?.headers?.each { String key, Object value ->
							header(key, value)
						}
					}
					body(yamlContract?.response?.body)
				}
			}]
		}
		catch (FileNotFoundException e) {
			throw new IllegalStateException(e)
		}
	}

	@Override
	public List<YamlContract> convertTo(Collection<Contract> contracts) {
		return contracts.collect { Contract contract ->
			YamlContract yamlContract = new YamlContract()
			yamlContract.request.with {
				method = contract?.request?.method?.clientValue
				url = contract?.request?.url?.clientValue
				headers = (contract?.request?.headers as Headers)?.asStubSideMap()
				body = contract?.request?.body?.clientValue as Map
			}
			yamlContract.response.with {
				status = contract?.response?.status?.clientValue as Integer
				headers = (contract?.response?.headers as Headers)?.asStubSideMap()
				body = contract?.response?.body?.clientValue as Map
			}
			return yamlContract
		}
	}
}
