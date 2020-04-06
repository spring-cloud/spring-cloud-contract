/*
 * Copyright 2013-2020 the original author or authors.
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

package org.springframework.cloud.contract.docs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.verifier.converter.YamlContract;
import org.springframework.cloud.contract.verifier.converter.YamlContractConverter;

class JsonSchemaTests {

	// @formatter:off
	private static final String CONTRACT = "description: Some description\n"
			+ "name: some name\n"
			+ "priority: 8\n"
			+ "ignored: true\n"
			+ "inProgress: true\n"
			+ "request:\n"
			+ "  method: PUT\n"
			+ "  url: /foo\n"
			+ "  queryParameters:\n"
			+ "    a: b\n"
			+ "    b: c\n"
			+ "  headers:\n"
			+ "    foo: bar\n"
			+ "    fooReq: baz\n"
			+ "  cookies:\n"
			+ "    foo: bar\n"
			+ "    fooReq: baz\n"
			+ "  body:\n"
			+ "    foo: bar\n"
			+ "  matchers:\n"
			+ "    body:\n"
			+ "      - path: $.foo\n"
			+ "        type: by_regex\n"
			+ "        value: bar\n"
			+ "    headers:\n"
			+ "      - key: foo\n"
			+ "        regex: bar\n"
			+ "response:\n"
			+ "  status: 200\n"
			+ "  fixedDelayMilliseconds: 1000\n"
			+ "  headers:\n"
			+ "    foo2: bar\n"
			+ "    foo3: foo33\n"
			+ "    fooRes: baz\n"
			+ "  body:\n"
			+ "    foo2: bar\n"
			+ "    foo3: baz\n"
			+ "    nullValue: null\n"
			+ "  matchers:\n"
			+ "    body:\n"
			+ "      - path: $.foo2\n"
			+ "        type: by_regex\n"
			+ "        value: bar\n"
			+ "      - path: $.foo3\n"
			+ "        type: by_command\n"
			+ "        value: executeMe($it)\n"
			+ "      - path: $.nullValue\n"
			+ "        type: by_null\n"
			+ "        value: null\n"
			+ "    headers:\n"
			+ "      - key: foo2\n"
			+ "        regex: bar\n"
			+ "      - key: foo3\n"
			+ "        command: andMeToo($it)\n"
			+ "    cookies:\n"
			+ "      - key: foo2\n"
			+ "        regex: bar\n"
			+ "      - key: foo3\n"
			+ "        predefined:\n";
	// @formatter:on

	@Test
	void should_produce_a_json_schema_of_a_yaml_model() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);
		JsonSchema schema = schemaGen.generateSchema(YamlContract.class);
		String schemaString = mapper.writeValueAsString(schema);
		File schemaFile = new File("target/contract_schema.json");
		Files.write(schemaFile.toPath(), schemaString.getBytes());
	}

	@Test
	void should_convert_yaml_to_contract() throws IOException {
		File ymlFile = new File("target/contract.yml");
		Files.write(ymlFile.toPath(), CONTRACT.getBytes());

		Collection<Contract> contracts = new YamlContractConverter().convertFrom(ymlFile);

		BDDAssertions.then(contracts).isNotEmpty();
	}

}
