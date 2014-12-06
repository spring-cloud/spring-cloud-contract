package eu.coderate.accurest

import static eu.coderate.accurest.builder.ClassBuilder.createClass
import static eu.coderate.accurest.builder.MethodBuilder.createVoidMethod

class Generator {

	public static void main(String[] args) {
		print createClass('MyTest', 'io.test', "com.test.BaseClass")
				.addImport('org.junit.Test')
				.addImport('org.junit.Rule')
				.addStaticImport('org.hamcrest.Matchers.*')
				.addStaticImport('com.jayway.restassured.RestAssured.*')
				.addStaticImport('com.jayway.restassured.matcher.RestAssuredMatchers.*')
				.addRule('com.test.MyRule')
				.addMethod(createVoidMethod('shouldInvokeService'))
				.build()
	}
}