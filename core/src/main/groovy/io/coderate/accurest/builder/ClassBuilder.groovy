package io.coderate.accurest.builder

import io.coderate.accurest.util.NamesUtil

/**
 * @author Jakub Kubrynski
 */
class ClassBuilder {

	private final String className
	private final String packageName
	private final String baseClass
	private final List<String> imports = []
	private final List<String> staticImports = []
	private final List<String> rules = []
	private final List<MethodBuilder> methods = []
	private final String modifier
	private final String suffix

	private ClassBuilder(String className, String packageName, String baseClass, TestFramework lang) {
		if (baseClass) {
			imports << baseClass
		}
		this.baseClass = NamesUtil.afterLastDot(baseClass)
		this.packageName = packageName
		this.className = className
		switch (lang) {
			case TestFramework.JUNIT:
				modifier = "public "
				suffix = ";"
				break
			case TestFramework.SPOCK:
				modifier = ""
				suffix = ""
		}
	}

	static ClassBuilder createClass(String className, String packageName, TestFramework lang) {
		return createClass(className, packageName, null, lang)
	}

	static ClassBuilder createClass(String className, String packageName, String baseClass, TestFramework lang) {
		return new ClassBuilder(className, packageName, baseClass, lang)
	}

	ClassBuilder addImport(String importToAdd) {
		imports << importToAdd
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

	ClassBuilder addRule(String ruleClass) {
		imports << ruleClass
		rules << NamesUtil.afterLastDot(ruleClass)
		return this
	}

	String build() {
		BlockBuilder clazz = new BlockBuilder("\t")
				.addLine("package $packageName" + suffix)
				.addEmptyLine()

		imports.sort().each {
			clazz.addLine("import $it" + suffix)
		}
		if (!imports.empty) {
			clazz.addEmptyLine()
		}

		staticImports.sort().each {
			clazz.addLine("import static $it" + suffix)
		}
		if (!staticImports.empty) {
			clazz.addEmptyLine()
		}

		def classLine = modifier + "class $className"
		if (baseClass) {
			classLine += " extends $baseClass"
		}
		clazz.addLine(classLine + ' {')
		clazz.addEmptyLine()

		clazz.startBlock()
		rules.sort().each {
			clazz.addLine("@Rule")
			clazz.addLine("public " + it + " " + NamesUtil.camelCase(it) + " = new $it()" + suffix)
		}
		clazz.endBlock()
		if (!rules.empty) {
			clazz.addEmptyLine()
		}

		methods.each {
			clazz.addBlock(it)
		}

		clazz.addLine('}')
		clazz.toString()
	}
}
