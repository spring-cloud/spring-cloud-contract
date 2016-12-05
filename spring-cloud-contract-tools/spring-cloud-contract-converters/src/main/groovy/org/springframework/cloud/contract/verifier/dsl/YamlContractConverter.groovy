package org.springframework.cloud.contract.verifier.dsl

import groovy.transform.CompileStatic
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.ContractConverter
import org.springframework.cloud.contract.spec.internal.Headers
import org.yaml.snakeyaml.Yaml

/**
 * Converter from and to a {@link YamlContract} to a
 * @author Marcin Grzejszczak
 * @since 1.0.3
 */
//TODO: Perform full conversion
@CompileStatic
class YamlContractConverter implements ContractConverter<YamlContract> {

	@Override
	public boolean isAccepted(File file) {
		String name = file.getName()
		return name.endsWith(".yml") || name.endsWith(".yaml")
	}

	@Override
	public Contract convertFrom(File file) {
		try {
			YamlContract yamlContract = new Yaml().loadAs(new FileInputStream(file), YamlContract.class)
			return Contract.make {
				request {
					method(yamlContract.request.method)
					url(yamlContract.request.url)
					headers {
						yamlContract.request.headers.each { String key, Object value ->
							header(key, value)
						}
					}
					body(yamlContract.request.body)
				}
				response {
					status(yamlContract.response.status)
					headers {
						yamlContract.response.headers.each { String key, Object value ->
							header(key, value)
						}
					}
					body(yamlContract.response.body)
				}
			}
		}
		catch (FileNotFoundException e) {
			throw new IllegalStateException(e)
		}
	}

	@Override
	public YamlContract convertTo(Contract contract) {
		// TODO: Pick one of the sides - consumer / producer
		YamlContract yamlContract = new YamlContract()
		yamlContract.request.with {
			method = contract.request.method.clientValue
			url = contract.request.url.clientValue
			headers = (contract.request.headers as Headers).asStubSideMap()
			body = contract.request.body.clientValue as Map
		}
		yamlContract.response.with {
			status = contract.response.status.clientValue as Integer
			headers = (contract.response.headers as Headers).asStubSideMap()
			body = contract.response.body.clientValue as Map
		}
		return yamlContract
	}
}
