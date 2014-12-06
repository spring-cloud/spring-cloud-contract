package eu.coderate.ck

import static eu.coderate.ck.builder.ClassBuilder.createClass
import static eu.coderate.ck.builder.MethodBuilder.createVoidMethod

class Generator {

	public static void main(String[] args) {
		print createClass('MyTest', 'io.test')
				.addImport('org.junit.Test')
				.addStaticImport('org.hamcrest.CoreMatchers.*')
				.addStaticImport('com.jayway.restassured.RestAssured.*')
				.addMethod(createVoidMethod('shouldInvokeService'))
				.build()
	}
}