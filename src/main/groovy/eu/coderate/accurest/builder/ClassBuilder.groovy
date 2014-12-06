package eu.coderate.accurest.builder

import eu.coderate.accurest.util.NamesUtil

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

	private ClassBuilder(String className, String packageName, String baseClass) {
		if (baseClass) {
			imports << baseClass
		}
		this.baseClass = NamesUtil.getName(baseClass)
		this.packageName = packageName
		this.className = className
	}

	static ClassBuilder createClass(String className, String packageName) {
		return createClass(className, packageName, null)
	}

	static ClassBuilder createClass(String className, String packageName, String baseClass) {
		return new ClassBuilder(className, packageName, baseClass)
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
		rules << NamesUtil.getName(ruleClass)
		return this
	}

	String build() {
		BlockBuilder clazz = new BlockBuilder("  ")
			.addLine("package $packageName;")
			.addEmptyLine()

		imports.sort().each {
			clazz.addLine("import $it;")
		}
		clazz.addEmptyLine()

		staticImports.sort().each {
			clazz.addLine("static import $it;")
		}
		clazz.addEmptyLine()

		def classLine = "public class $className"
		if (baseClass) {
			classLine += " extends $baseClass"
		}
		clazz.addLine(classLine + ' {')
		clazz.addEmptyLine()

		clazz.startBlock()
		rules.sort().each {
			clazz.addLine("@Rule")
			clazz.addLine("public " + it + " " + NamesUtil.fieldName(it) + " = new $it();")
		}
		clazz.endBlock()
		clazz.addEmptyLine()

		methods.each {
			clazz.addBlock(it)
		}

		clazz.addLine('}')
		clazz.toString()
	}
}
