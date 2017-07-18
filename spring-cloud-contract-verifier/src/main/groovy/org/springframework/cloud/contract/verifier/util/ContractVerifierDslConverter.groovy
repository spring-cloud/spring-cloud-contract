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

package org.springframework.cloud.contract.verifier.util

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.codehaus.groovy.control.CompilerConfiguration
import org.springframework.cloud.contract.spec.Contract

/**
 * Converts a file or String into a {@link Contract}
 *
 * @author Marcin Grzejszczak
 *
 * @since 1.0.0
 */
@CompileStatic
@Slf4j
class ContractVerifierDslConverter {

	/**
	 * @deprecated - use {@link ContractVerifierDslConverter#convertAsCollection(java.io.File, java.lang.String)}
	 */
	@Deprecated
	static Collection<Contract> convertAsCollection(String dsl) {
		try {
			Object object = groovyShell().evaluate(dsl)
			return listOfContracts(object)
		} catch (DslParseException e) {
			throw e
		} catch (Exception e) {
			log.error("Exception occurred while trying to evaluate the contract", e)
			throw new DslParseException(e)
		}
	}

	static Collection<Contract> convertAsCollection(File rootFolder, String dsl) {
		ClassLoader classLoader = ContractVerifierDslConverter.getClassLoader()
		try {
			ClassLoader urlCl = updatedClassLoader(rootFolder, classLoader)
			Object object = groovyShell(urlCl, rootFolder).evaluate(dsl)
			return listOfContracts(object)
		} catch (DslParseException e) {
			throw e
		} catch (Exception e) {
			log.error("Exception occurred while trying to evaluate the contract", e)
			throw new DslParseException(e)
		} finally {
			Thread.currentThread().setContextClassLoader(classLoader)
		}
	}

	/**
	 * @deprecated - use {@link ContractVerifierDslConverter#convertAsCollection(java.io.File, java.io.File)}
	 */
	@Deprecated
	static Collection<Contract> convertAsCollection(File dsl) {
		try {
			Object object = groovyShell().evaluate(dsl)
			return listOfContracts(object)
		} catch (DslParseException e) {
			throw e
		} catch (Exception e) {
			log.error("Exception occurred while trying to evaluate the contract at path [${dsl.path}]", e)
			throw new DslParseException(e)
		}
	}

	static Collection<Contract> convertAsCollection(File rootFolder, File dsl) {
		ClassLoader classLoader = ContractVerifierDslConverter.getClassLoader()
		try {
			ClassLoader urlCl = updatedClassLoader(rootFolder, classLoader)
			Object object = groovyShell(urlCl, rootFolder).evaluate(dsl)
			return listOfContracts(object)
		} catch (DslParseException e) {
			throw e
		} catch (Exception e) {
			log.error("Exception occurred while trying to evaluate the contract at path [${dsl.path}]", e)
			throw new DslParseException(e)
		} finally {
			Thread.currentThread().setContextClassLoader(classLoader)
		}
	}

	private static ClassLoader updatedClassLoader(File rootFolder, ClassLoader classLoader) {
		ClassLoader urlCl = URLClassLoader
				.newInstance([rootFolder.toURI().toURL()] as URL[], classLoader)
		Thread.currentThread().setContextClassLoader(urlCl)
		return urlCl
	}

	private static GroovyShell groovyShell() {
		return new GroovyShell(ContractVerifierDslConverter.classLoader, new CompilerConfiguration(sourceEncoding: 'UTF-8'))
	}

	private static GroovyShell groovyShell(ClassLoader cl, File rootFolder) {
		return new GroovyShell(cl,
				new CompilerConfiguration(sourceEncoding: 'UTF-8',
						classpathList: [rootFolder.absolutePath]))
	}

	private static Collection<Contract> listOfContracts(object) {
		if (object instanceof Collection) {
			return object as Collection<Contract>
		} else if (!object instanceof Contract) {
			throw new DslParseException("Contract is not returning a Contract or list of Contracts")
		}
		return [object] as Collection<Contract>
	}
}
