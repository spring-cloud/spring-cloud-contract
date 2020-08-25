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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.verifier.converter.YamlContract;
import org.springframework.cloud.contract.verifier.converter.YamlContractConverter;
import org.springframework.cloud.contract.verifier.util.SpringCloudContractMetadata;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

/**
 * This test generates additional resources in the `target` folder that are then
 * referenced by the documentation.
 */
class AdditionalResourcesGenerationTests {

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
		String schemaString = generateJsonSchemaForClass(YamlContract.class);
		File schemaFile = new File("target/contract_schema.json");
		Files.write(schemaFile.toPath(), schemaString.getBytes());
	}

	private String generateJsonSchemaForClass(Class clazz)
			throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);
		JsonSchema schema = schemaGen.generateSchema(clazz);
		return mapper.writeValueAsString(schema);
	}

	@Test
	void should_convert_yaml_to_contract() throws IOException {
		File ymlFile = new File("target/contract.yml");
		Files.write(ymlFile.toPath(), CONTRACT.getBytes());

		Collection<Contract> contracts = new YamlContractConverter().convertFrom(ymlFile);

		BDDAssertions.then(contracts).isNotEmpty();
	}

	@Test
	void should_produce_an_adoc_with_all_of_metadata_classes() throws Exception {
		List<Class> metadata = metadataClasses();
		File doc = new File("target/metadata.adoc");

		StringBuilder sb = adocWithMetadata(metadata);

		Files.write(doc.toPath(), sb.toString().getBytes());
	}

	private StringBuilder adocWithMetadata(List<Class> metadata) throws Exception {
		YAMLMapper mapper = new YAMLMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
		StringBuilder sb = new StringBuilder();
		for (Class metadatum : metadata) {
			SpringCloudContractMetadata newInstance = (SpringCloudContractMetadata) metadatum
					.newInstance();
			String description = newInstance.description();
			String key = newInstance.key();
			List<Class> additionalClasses = classesToLookAt(metadatum, newInstance);
			// @formatter:off
			sb
				.append("[[metadata-").append(key).append("]]\n")
				.append("##### Metadata `").append(key).append("`\n\n")
				.append("* key: `").append(key).append("`").append("\n")
				.append("* description:\n\n").append(description).append("\n\n")
				.append("Example:\n\n")
				.append("```yaml\n").append(mapper.writeValueAsString(newInstance)).append("\n```\n\n")
				// To make the schema collapsable
				.append("+++ <details><summary> +++\nClick here to expand the JSON schema:\n+++ </summary><div> +++\n")
				.append("```json\n").append(generateJsonSchemaForClass(metadatum)).append("\n```\n")
				.append("+++ </div></details> +++\n\n")
				.append("If you are interested in learning more about the types and its properties, check out the following classes:\n\n")
				.append(additionalClasses.stream().map(aClass -> "* `" + aClass.getName() + "`").collect(Collectors.joining("\n")))
				.append("\n\n");
			// @formatter:on
		}
		return sb;
	}

	private List<Class> classesToLookAt(Class metadatum,
			SpringCloudContractMetadata newInstance) {
		List<Class> additionalClasses = new ArrayList<>();
		additionalClasses.add(metadatum);
		additionalClasses.addAll(newInstance.additionalClassesToLookAt());
		return additionalClasses;
	}

	private List<Class> metadataClasses() throws ClassNotFoundException {
		BeanDefinitionRegistry bdr = new SimpleBeanDefinitionRegistry();
		ClassPathBeanDefinitionScanner s = new ClassPathBeanDefinitionScanner(bdr, false);
		TypeFilter tf = new AssignableTypeFilter(SpringCloudContractMetadata.class);
		s.addIncludeFilter(tf);
		String basePackage = "org.springframework.cloud.contract";
		s.scan(basePackage);
		String[] beans = bdr.getBeanDefinitionNames();
		List<Class> metadata = new ArrayList<>();
		for (String bean : beans) {
			BeanDefinition beanDefinition = bdr.getBeanDefinition(bean);
			String beanClassName = beanDefinition.getBeanClassName();
			if (beanClassName != null && !beanClassName.contains(basePackage)) {
				continue;
			}
			metadata.add(Class.forName(beanClassName));
		}
		return metadata;
	}

}
