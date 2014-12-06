package eu.coderate.ck.builder

/**
 * @author Jakub Kubrynski
 */
class ClassBuilder {

	private final String className
	private final String packageName
	private final List<String> imports = []
	private final List<String> staticImports = []
	private final List<MethodBuilder> methods = []

	private ClassBuilder(String className, String packageName) {
		this.packageName = packageName
		this.className = className
	}

	static ClassBuilder createClass(String className, String packageName) {
		return new ClassBuilder(className, packageName)
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

		clazz.addLine("public class $className {")
		clazz.addEmptyLine()

		methods.each {
			clazz.addBlock(it)
		}

		clazz.addLine('}')
		clazz.toString()
	}
}
