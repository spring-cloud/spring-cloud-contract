package org.springframework.cloud.contract.verifier.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ClassToBuild {
	private ClassMetaData metaData;
	private List<Import> imports = new ArrayList<>();
	private List<StaticImport> staticImports = new ArrayList<>();
	private List<ClassAnnotation> annotations = new ArrayList<>();
	private List<Field> fields = new ArrayList<>();
	private List<Method> methods = new ArrayList<Method>();
}

interface ClassMetaData {
	ClassMetaData packageDefinition();
	ClassMetaData modifier();
	ClassMetaData fileExtension();
	ClassMetaData suffix();
	ClassMetaData lineEnding();
	ClassMetaData parentClass();
}

interface ClassAnnotation {
	ClassAnnotation annotation();
}

interface Import {
	Import anImport();
}

interface StaticImport {
	Import staticImport();
}

interface Field {
	Field annotations();
	Field modifier();
	Field field();
}

interface Given {
	Method doSth();
}

interface MethodAnnotations {
	Method methodAnnotations();
}

// JSON
// HTTP
// MOCK MVC [standalone, mockmvc, webclient]
// Scenario
// Custom rule

// JUNIT4

// ClassMetaData -> new JavaClassMetaData()

// Imports -> new Junit4JsonMockMvcImports()
	// Imports -> new Junit4Imports()
	// Imports -> new JsonImports()
	// Imports -> new MockMvcImports()

// ClassAnnotation -> new Junit4ClassAnnotation()



interface Method {
	MethodAnnotations annotations();
	Method modifier();
	Given given();
	When when();
	Then then();

	default void foo() {
		Method foo;
		foo.annotations()
				.methodAnnotations()
			.modifier()
				.given()
					.doSth()
				.when();
	}
}

interface Assertions {
}

interface Then {
	Assertions assertions();
}

interface When {

}
