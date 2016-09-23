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

package org.springframework.cloud.contract.verifier.builder

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.springframework.cloud.contract.verifier.config.TestFramework
import org.springframework.cloud.contract.verifier.util.NamesUtil
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties

/**
 * Builds a class. Adds all the imports, static imports etc.
 *
 * @author Jakub Kubrynski, codearte.io
 *
 * @since 1.0.0
 */
@CompileStatic
@PackageScope
class ClassBuilder {

	private final String className
	private final String packageName
	private final String baseClass
	private final List<String> imports = []
	private final List<String> staticImports = []
	private final List<String> rules = []
	private final List<String> fields = []
	private final List<MethodBuilder> methods = []
	private final List<String> classLevelAnnotations = []
	private final TestFramework lang

	private ClassBuilder(String className, String packageName, String baseClass, TestFramework lang) {
		this.lang = lang
		if (baseClass) {
			imports << baseClass
		}
		this.baseClass = NamesUtil.afterLastDot(baseClass)
		this.packageName = packageName
		this.className = className
	}

	/**
	 * Returns a {@link ClassBuilder} for the given parameters
	 */
	static ClassBuilder createClass(String className, String classPackage, ContractVerifierConfigProperties properties,
									String includedDirectoryRelativePath) {
		String baseClassForTests
		if (properties.targetFramework == TestFramework.SPOCK && !properties.baseClassForTests
				&& !properties.packageWithBaseClasses && !properties.baseClassMappings) {
			baseClassForTests = 'spock.lang.Specification'
		} else {
			baseClassForTests = retrieveBaseClass(properties, includedDirectoryRelativePath)
		}
		return new ClassBuilder(className, classPackage, baseClassForTests, properties.targetFramework)
	}

	protected static String retrieveBaseClass(ContractVerifierConfigProperties properties, String includedDirectoryRelativePath) {
		String contractPackage = includedDirectoryRelativePath.replace(File.separator, '.')
		// package mapping takes super precedence
		if (properties.baseClassMappings) {
			Map.Entry<String, String> mapping = properties.baseClassMappings.find { String pattern, String fqn ->
				return contractPackage.matches(pattern)
			}
			if (mapping) {
				return mapping.value
			}
		}
		if (!properties.packageWithBaseClasses) {
			return properties.baseClassForTests
		}
		String generatedClassName = generateDefaultBaseClassName(contractPackage, properties)
		return "${generatedClassName}Base"
	}

	private static String generateDefaultBaseClassName(String classPackage, ContractVerifierConfigProperties properties) {
		String[] splitPackage = classPackage.split("\\.")
		if (splitPackage.size() > 1) {
			String last = NamesUtil.capitalize(splitPackage[-1])
			String butLast = NamesUtil.capitalize(splitPackage[-2])
			return "${properties.packageWithBaseClasses}.${butLast}${last}"
		}
		return "${properties.packageWithBaseClasses}.${NamesUtil.capitalize(splitPackage[0])}"
	}

	ClassBuilder addImport(String importToAdd) {
		imports << importToAdd
		return this
	}

	ClassBuilder addImport(List<String> importsToAdd) {
		imports.addAll(importsToAdd)
		return this
	}

	ClassBuilder addStaticImport(String importToAdd) {
		staticImports << importToAdd
		return this
	}

	ClassBuilder addMethod(MethodBuilder methodBuilder) {
		methods << methodBuilder
		return this
	}

	ClassBuilder addField(String fieldToAdd) {
		fields << appendColonIfJUniTest(fieldToAdd)
		return this
	}

	private String appendColonIfJUniTest(String field) {
		if (lang == TestFramework.JUNIT && !field.endsWith(';')) {
			return "$field;"
		}
		return field
	}

	ClassBuilder addField(List<String> fieldsToAdd) {
		fields.addAll(fieldsToAdd.collect { appendColonIfJUniTest(it) })
		return this
	}

	ClassBuilder addRule(String ruleClass) {
		imports << ruleClass
		rules << NamesUtil.afterLastDot(ruleClass)
		return this
	}

	String build() {
		BlockBuilder clazz = new BlockBuilder("\t")
				.addLine("package $packageName$lang.lineSuffix")
				.addEmptyLine()

		imports.sort().each {
			clazz.addLine("import $it$lang.lineSuffix")
		}
		if (!imports.empty) {
			clazz.addEmptyLine()
		}

		staticImports.sort().each {
			clazz.addLine("import static $it$lang.lineSuffix")
		}
		if (!staticImports.empty) {
			clazz.addEmptyLine()
		}

		classLevelAnnotations.sort().each {
			clazz.addLine(it)
		}

		def classLine = "${lang.classModifier}class $className"
		if (baseClass) {
			classLine += " extends $baseClass"
		}
		clazz.addLine(classLine + ' {')
		clazz.addEmptyLine()

		clazz.startBlock()
		rules.sort().each {
			clazz.addLine("@Rule")
			clazz.addLine("public $it ${NamesUtil.camelCase(it)} = new $it()$lang.lineSuffix")
		}
		clazz.endBlock()
		if (!rules.empty) {
			clazz.addEmptyLine()
		}

		clazz.startBlock()
		fields.sort().each {
			clazz.addLine(it)
		}
		if (!fields.empty) {
			clazz.addEmptyLine()
		}
		clazz.endBlock()

		methods.each {
			clazz.addBlock(it)
		}

		clazz.addLine('}')
		clazz.toString()
	}

	void addClassLevelAnnotation(String annotation) {
		classLevelAnnotations << annotation
	}
}
