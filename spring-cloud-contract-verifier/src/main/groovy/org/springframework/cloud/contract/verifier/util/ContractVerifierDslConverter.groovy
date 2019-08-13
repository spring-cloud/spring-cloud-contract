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

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.ContractConverter
import org.springframework.cloud.function.compiler.java.CompilationResult
import org.springframework.cloud.function.compiler.java.RuntimeJavaCompiler
import org.springframework.util.StringUtils

/**
 * Converts a String or a Groovy or Java file into a {@link Contract}.
 *
 * @author Marcin Grzejszczak
 *
 * @since 1.0.0
 */
@CompileStatic
@Commons
class ContractVerifierDslConverter implements ContractConverter<Collection<Contract>> {

	public static final ContractVerifierDslConverter INSTANCE = new ContractVerifierDslConverter()

	private static final Pattern PACKAGE_PATTERN = Pattern.compile(".+?package (.+?);.+?", Pattern.DOTALL)

	private static final Pattern CLASS_PATTERN = Pattern.compile(".+?class (.+?)( |\\{).+?", Pattern.DOTALL)

	private static final RuntimeJavaCompiler COMPILER = new RuntimeJavaCompiler()

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
		updateTheThreadClassLoader(urlCl)
		return urlCl
	}

	private static void updateTheThreadClassLoader(ClassLoader urlCl) {
		Thread.currentThread().setContextClassLoader(urlCl)
	}

	private static GroovyShell groovyShell() {
		return new GroovyShell(ContractVerifierDslConverter.classLoader, new CompilerConfiguration(sourceEncoding: 'UTF-8'))
	}

	private static Object toObject(ClassLoader cl, File rootFolder, File dsl) {
		if (isJava(dsl)) {
			try {
				return parseJavaFile(cl, dsl)
			}
			catch (Exception ex) {
				if (log.isWarnEnabled()) {
					log.warn("Exception occurred while trying to parse the file [" + dsl + "] as a contract. Will not parse it.", ex)
				}
				return null
			}
		}
		return groovyShell(cl, rootFolder).evaluate(dsl)
	}

	private static Object parseJavaFile(ClassLoader cl, File dsl) {
		Constructor<?> constructor = classConstructor(dsl)
		Object newInstance = constructor.newInstance()
		if (!newInstance instanceof Supplier) {
			if (log.isDebugEnabled()) {
				log.debug("The class [" + dsl + "] is not instance of Supplier. Will not parse it as a contract")
			}
			return null
		}
		Supplier supplier = (Supplier) newInstance
		return supplier.get()
	}

	private static Constructor<?> classConstructor(File dsl) {
		String classText = dsl.text
		String fqn = fqn(classText)
		CompilationResult compilationResult = COMPILER
				.compile(fqn, classText)
		if (!compilationResult.wasSuccessful()) {
			throw new IllegalStateException("Exceptions occurred while trying to compile the file " + compilationResult.compilationMessages)
		}
		Class<?> clazz = compilationResult.compiledClasses.find { it.name == fqn}
		if (clazz == null) {
			throw new IllegalStateException("Class with name [" + fqn + "] not found")
		}
		Constructor<?> constructor = clazz.getDeclaredConstructor()
		constructor.setAccessible(true)
		return constructor
	}

	private static boolean isJava(File dsl) {
		return dsl.name.endsWith(".java")
	}

	private static String fqn(String classText) {
		Matcher packageMatcher = PACKAGE_PATTERN.matcher(classText)
		String fqn = "";
		if (packageMatcher.matches()) {
			fqn = packageMatcher.group(1) + "."
		}
		Matcher classMatcher = CLASS_PATTERN.matcher(classText)
		if (!classMatcher.matches()) {
			throw new IllegalAccessException("Can't parse the class name")
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
		if (object == null) {
			return Collections.emptyList()
		}
		else if (isACollectionOfContracts(object)) {
			return withName(file, object as Collection<Contract>)
		}
		else if (!object instanceof Contract) {
			throw new DslParseException("Contract is not returning a Contract or list of Contracts")
		}
		return withName(file, [object] as Collection<Contract>)
	}

	private static boolean isACollectionOfContracts(object) {
		return object instanceof Collection && ((Collection) object).every { it instanceof Contract }
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

	@Override
	boolean isAccepted(File file) {
		return file.name.endsWith(".groovy") || file.name.endsWith(".gvy") || file.name.endsWith(".java")
	}

	@Override
	Collection<Contract> convertFrom(File file) {
		return convertAsCollection(file)
	}

	@Override
	Collection<Contract> convertTo(Collection<Contract> contract) {
		return contract
	}
}
