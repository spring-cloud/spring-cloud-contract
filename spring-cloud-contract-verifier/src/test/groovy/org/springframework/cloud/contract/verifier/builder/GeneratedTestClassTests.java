package org.springframework.cloud.contract.verifier.builder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

import org.assertj.core.api.BDDAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties;
import org.springframework.cloud.contract.verifier.file.ContractMetadata;
import org.springframework.util.FileSystemUtils;

import static org.springframework.cloud.contract.verifier.util.ContractVerifierDslConverter.convertAsCollection;

public class GeneratedTestClassTests {

	// @formatter:off
	String contract = "org.springframework.cloud.contract.spec.Contract.make {\n"
			+ " request {\n"
			+ "  method 'PUT'\n"
			+ "  url 'url'\n"
			+ "  headers {\n"
			+ "    header('foo', 'bar')\n"
			+ "  }\n"
			+ "  body (\n"
			+ "    [\"foo\":\"bar\"]\n"
			+ "  )\n"
			+ " }\n"
			+ " response {\n"
			+ "  status OK()\n"
			+ " }\n"
			+ "}";
	// @formatter:on

	@Rule
	public TemporaryFolder tmpFolder = new TemporaryFolder();

	File file;

	File tmp;

	@Before
	public void setup() throws IOException, URISyntaxException {
		this.file = this.tmpFolder.newFile();
		Files.write(this.file.toPath(), this.contract.getBytes());
		this.tmp = this.tmpFolder.newFolder();
		File classpath = new File(
				GeneratedTestClassTests.class.getResource("/classpath/").toURI());
		FileSystemUtils.copyRecursively(classpath, this.tmp);
	}

	@Test
	public void should_work_for_junit4_mockmvc_json_non_binary() {
		// given
		RefactoredSingleTestGenerator generator = new RefactoredSingleTestGenerator();
		ContractVerifierConfigProperties configProperties = new ContractVerifierConfigProperties();
		Collection<ContractMetadata> contracts = Collections
				.singletonList(new ContractMetadata(this.file.toPath(), true, 1, 2,
						convertAsCollection(new File("/"), this.file)));
		String includedDirectoryRelativePath = "some/path";
		String convertedClassName = "fooBar";
		String packageName = "test";
		Path classPath = new File("/tmp").toPath();
		configProperties.setBaseClassForTests("BazBar");

		// when
		String builtClass = generator.buildClass(configProperties, contracts,
				includedDirectoryRelativePath, new SingleTestGenerator.GeneratedClassData(
						convertedClassName, packageName, classPath));

		// then
		System.out.println(builtClass);
		BDDAssertions.then(builtClass).isNotEmpty();
	}

	/*

	package test;

	import com.jayway.jsonpath.DocumentContext;
	import com.jayway.jsonpath.JsonPath;
	import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
	import io.restassured.response.ResponseOptions;
	import java.io.StringReader;
	import javax.xml.parsers.DocumentBuilder;
	import javax.xml.parsers.DocumentBuilderFactory;
	import org.junit.FixMethodOrder;
	import org.junit.Ignore;
	import org.junit.Test;
	import org.junit.runners.MethodSorters;
	import org.w3c.dom.Document;
	import org.xml.sax.InputSource;

	import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson;
	import static io.restassured.module.mockmvc.RestAssuredMockMvc.*;
	import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat;
	import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.*;

	@FixMethodOrder(MethodSorters.NAME_ASCENDING)
	public class Test {

		@Test
		@Ignore
		public void validate_junit7345461633712967() throws Exception {
			// given:
				MockMvcRequestSpecification request = given();

			// when:
				ResponseOptions response = given().spec(request)
						.put("url");

			// then:
				assertThat(response.statusCode()).isEqualTo(200);
		}

	}


	 */

}