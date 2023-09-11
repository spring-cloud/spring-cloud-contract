/*
 * Copyright 2012-2020 the original author or authors.
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
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.cloud.contract.verifier.converter.YamlContract;
import org.springframework.cloud.contract.verifier.util.SpringCloudContractMetadata;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

/**
 * @author Marcin Grzejszczak
 */
public class Main {

	private static final Logger log = LoggerFactory.getLogger(Main.class);

	private final File rootPath;

	public Main(File rootPath) {
		this.rootPath = rootPath;
	}

	public static void main(String... args) throws Exception {
		File rootPath = new File(args[0]);
		Main main = new Main(rootPath);
		main.produceJsonSchemaOfAYamlModel();
		main.produceAdocWithAllOfMetadataClasses();
	}

	void produceJsonSchemaOfAYamlModel() throws IOException {
		log.info("Generating schema...");
		String schemaString = generateJsonSchemaForClass(YamlContract.class);
		File schemaFile = new File(this.rootPath, "modules/ROOT/partials/contract_schema.json");
		Files.write(schemaFile.toPath(), schemaString.getBytes());
		log.info("Generated schema!");
	}

	private String generateJsonSchemaForClass(Class clazz) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);
		JsonSchema schema = schemaGen.generateSchema(clazz);
		return mapper.writeValueAsString(schema);
	}

	void produceAdocWithAllOfMetadataClasses() throws Exception {
		log.info("Produce adoc with all metadata...");
		List<Class> metadata = metadataClasses();
		File doc = new File(this.rootPath, "target/metadata.adoc");
		StringBuilder sb = adocWithMetadata(metadata);
		Files.write(doc.toPath(), sb.toString().getBytes());
		log.info("Produced adoc with all metadata!");
	}

	private StringBuilder adocWithMetadata(List<Class> metadata) throws Exception {
		YAMLMapper mapper = new YAMLMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
		StringBuilder sb = new StringBuilder();
		for (Class metadatum : metadata) {
			Constructor constructor = null;
			try {
				constructor = metadatum.getDeclaredConstructor(null);
			}
			catch (Exception ex) {
				log.warn("Failed to find a no-args constructor for matadatum [" + metadatum + "]", ex);
				continue;
			}
			Object instance = constructor.newInstance();
			SpringCloudContractMetadata newInstance = (SpringCloudContractMetadata) instance;
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

	private List<Class> classesToLookAt(Class metadatum, SpringCloudContractMetadata newInstance) {
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
