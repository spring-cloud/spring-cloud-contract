package io.codearte.accurest.builder

import io.codearte.accurest.config.AccurestConfigProperties
import io.codearte.accurest.config.TestFramework
import io.codearte.accurest.util.NamesUtil
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

	static ClassBuilder createClass(String className, String classPackage, AccurestConfigProperties properties) {
		String baseClassForTests
		if (properties.targetFramework == TestFramework.SPOCK && !properties.baseClassForTests) {
			baseClassForTests = 'spock.lang.Specification'
		} else {
			baseClassForTests = properties.baseClassForTests
		}
		return new ClassBuilder(className, classPackage, baseClassForTests, properties.targetFramework)
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

		fields.sort().each {
			clazz.addLine(it)
		}
		if (!fields.empty) {
			clazz.addEmptyLine()
		}

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
