/*
 * Copyright 2013-2019 the original author or authors.
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

package org.springframework.cloud.contract.verifier.util

import java.lang.reflect.Constructor
import java.util.function.Supplier
import java.util.regex.Matcher
import java.util.regex.Pattern

import groovy.transform.CompileStatic
import groovy.util.logging.Commons
import org.codehaus.groovy.control.CompilerConfiguration
import org.mdkt.compiler.InMemoryJavaCompiler

import org.springframework.cloud.contract.spec.Contract
import org.springframework.util.StringUtils
/**
 * Converts a file or String into a {@link Contract}
 *
 * @author Marcin Grzejszczak
 *
 * @since 1.0.0
 */
@CompileStatic
@Commons
class ContractVerifierDslConverter {

	private static final InMemoryJavaCompiler IN_MEMORY_COMPILER = InMemoryJavaCompiler.newInstance()

	/**
	 * @deprecated - use {@link ContractVerifierDslConverter#convertAsCollection(java.io.File, java.lang.String)}
	 */
	@Deprecated
	static Collection<Contract> convertAsCollection(String dsl) {
		try {
			Object object = groovyShell().evaluate(dsl)
			return listOfContracts(object)
		}
		catch (DslParseException e) {
			throw e
		}
		catch (Exception e) {
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
		}
		catch (DslParseException e) {
			throw e
		}
		catch (Exception e) {
			log.error("Exception occurred while trying to evaluate the contract", e)
			throw new DslParseException(e)
		}
		finally {
			Thread.currentThread().setContextClassLoader(classLoader)
		}
	}

	static Collection<Contract> convertAsCollection(File dsl) {
		return convertAsCollection(dsl.parentFile, dsl)
	}

	static Collection<Contract> convertAsCollection(File rootFolder, File dsl) {
		ClassLoader classLoader = ContractVerifierDslConverter.getClassLoader()
		try {
			ClassLoader urlCl = updatedClassLoader(rootFolder, classLoader)
			Object object = toObject(urlCl, rootFolder, dsl)
			return listOfContracts(dsl, object)
		}
		catch (DslParseException e) {
			throw e
		}
		catch (Exception e) {
			log.error("Exception occurred while trying to evaluate the contract at path [${dsl.path}]", e)
			throw new DslParseException(e)
		}
		finally {
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

	private static Object toObject(ClassLoader cl, File rootFolder, File dsl) {
		if (dsl.name.endsWith(".groovy") || dsl.name.endsWith(".gvy")) {
			return groovyShell(cl, rootFolder).evaluate(dsl)
		} else if (dsl.name.endsWith(".java")) {
			String classText = new String(new FileInputStream(dsl).readAllBytes())
			String fqn = fqn(classText)
			Class<?> clazz = IN_MEMORY_COMPILER.compile(fqn, classText)
			Constructor<?> constructor = clazz.getDeclaredConstructor()
			constructor.setAccessible(true)
			Object newInstance = constructor.newInstance()
			if (!newInstance instanceof Supplier) {
				// TODO: Throw exception
			}
			Supplier supplier = (Supplier) newInstance
			return supplier.get()
		}
		throw new UnsupportedOperationException("Unsupported file type for file [" + dsl + "]")
	}

	private static String fqn(String classText) {
		Pattern packagePattern = Pattern.compile(".+?package (.+?);.+?", Pattern.DOTALL)
		Matcher packageMatcher = packagePattern.matcher(classText)
		String fqn = "";
		if (packageMatcher.matches()) {
			fqn = packageMatcher.group(1) + "."
		}
		Pattern classPattern = Pattern.compile(".+?class (.+?)( |\\{).+?", Pattern.DOTALL)
		Matcher classMatcher = classPattern.matcher(classText)
		if (!classMatcher.matches()) {
			// TODO: Throw an exception
		}
		return fqn + classMatcher.group(1)
	}

	private static GroovyShell groovyShell(ClassLoader cl, File rootFolder) {
		return new GroovyShell(cl,
				new CompilerConfiguration(sourceEncoding: 'UTF-8',
						classpathList: [rootFolder.absolutePath]))
	}

	private static Collection<Contract> listOfContracts(object) {
		if (object instanceof Collection) {
			return object as Collection<Contract>
		}
		else if (!object instanceof Contract) {
			throw new DslParseException("Contract is not returning a Contract or list of Contracts")
		}
		return [object] as Collection<Contract>
	}

	private static Collection<Contract> listOfContracts(File file, Object object) {
		if (object instanceof Collection) {
			return withName(file, object as Collection<Contract>)
		}
		else if (!object instanceof Contract) {
			throw new DslParseException("Contract is not returning a Contract or list of Contracts")
		}
		return withName(file, [object] as Collection<Contract>)
	}

	private static Collection<Contract> withName(File file, Collection<Contract> contracts) {
		int counter = 0
		return contracts.collect {
			if (contractNameEmpty(it)) {
				it.name(NamesUtil.defaultContractName(file, contracts, counter))
			}
			counter++
			return it
		}
	}

	private static boolean contractNameEmpty(Contract it) {
		return it != null && StringUtils.isEmpty(it.name)
	}
}
