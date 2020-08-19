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

package contracts;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.gradle.api.Project;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.JavaUnit;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaDocSource;

public class DocsFromSources {

	private static final String ADOC_HEADER =
			".Docker environment variables - read at runtime\n"
			+ "|===\n"
			+ "|Name | Description | Default\n";

	private final Project project;

	public DocsFromSources(Project project) {
		this.project = project;
	}

	public void buildApplicationEnvVars() {
		Path path = new File(rootDir(), sourcePath()).toPath();
		List<EnvVar> envVars = new ArrayList<>();
		FileVisitor<Path> fv = new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
					throws IOException {
				if (!file.toString().endsWith(".java")) {
					info("Skipping [" + file.toString() + "]");
					return FileVisitResult.CONTINUE;
				}
				JavaUnit unit = Roaster.parseUnit(Files.newInputStream(file));
				JavaClassSource myClass = unit.getGoverningType();
				info("Checking [" + myClass.getName() + "]");
				List<FieldSource<JavaClassSource>> fields = myClass.getFields();
				for (FieldSource<JavaClassSource> field : fields) {
					List<AnnotationSource<JavaClassSource>> annotations = field.getAnnotations();
					for (AnnotationSource<JavaClassSource> annotation : annotations) {
						if ("org.springframework.beans.factory.annotation.Value".equals(annotation.getQualifiedName())) {
							info("Field [" + field.getName() + "] has @Value annotation");
							JavaDocSource<FieldSource<JavaClassSource>> javaDoc = field.getJavaDoc();
							String description = javaDoc.getFullText();
							// ${foo:asd}
							String annotationValue = annotation.getStringValue();
							// foo:asd
							annotationValue = annotationValue.substring(2, annotationValue.length() - 1);
							String[] parsed = annotationValue.split(":");
							String defaultValue = "";
							String name = parsed[0];
							if (parsed.length == 2) {
								defaultValue = parsed[1];
							}
							envVars.add(new EnvVar(name, description, defaultValue));
						}
					}
				}
				info("Found [" + envVars.size() + "] env var field entries");
				return FileVisitResult.CONTINUE;
			}
		};

		try {
			Files.walkFileTree(path, fv);
			Path output = new File(rootDir(), "build/appProps.adoc").toPath();
			StringBuilder stringBuilder = new StringBuilder().append(ADOC_HEADER);
			Collections.sort(envVars);
			envVars.forEach(envVar -> stringBuilder.append(envVar.toString()).append("\n"));
			stringBuilder.append("|===");
			Files.write(output, stringBuilder.toString().getBytes());
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	String sourcePath() {
		return "src/test/java/contracts";
	}

	File rootDir() {
		return project.getRootDir();
	}

	void info(String log) {
		this.project.getLogger().info(log);
	}
}

class EnvVar implements Comparable<EnvVar> {
	final String name;
	final String description;
	final String defaultValue;

	EnvVar(String name, String description, String defaultValue) {
		this.name = name;
		this.description = description;
		this.defaultValue = defaultValue;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		EnvVar envVar = (EnvVar) o;
		return Objects.equals(name, envVar.name) &&
				Objects.equals(description, envVar.description) &&
				Objects.equals(defaultValue, envVar.defaultValue);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, description, defaultValue);
	}

	@Override
	public int compareTo(EnvVar o) {
		return name.compareTo(o.name);
	}

	@Override
	public String toString() {
		return "|" + name + "|" + description + "|" + defaultValue;
	}
}