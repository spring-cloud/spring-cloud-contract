/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.wiremock.file;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import com.github.tomakehurst.wiremock.common.BinaryFile;
import com.github.tomakehurst.wiremock.common.ClasspathFileSource;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.common.TextFile;

/**
 * @author Dave Syer
 * @author Pei-Tang Huang
 */
public class ResourcesFileSource implements FileSource {

	private final FileSource[] sources;

	public ResourcesFileSource(Resource... resources) {
		this(toSources(resources));
	}

	private ResourcesFileSource(FileSource... sources) {
		this.sources = sources;
	}

	private static FileSource[] toSources(Resource[] resources) {
		FileSource[] sources = new FileSource[resources.length];
		for (int i = 0; i < resources.length; i++) {
			Resource resource = resources[i];
			if (resource instanceof ClassPathResource) {
				ClassPathResource classes = (ClassPathResource) resource;
				sources[i] = new ClasspathFileSource(classes.getPath());
			}
			else if (resource instanceof FileSystemResource) {
				FileSystemResource files = (FileSystemResource) resource;
				sources[i] = new SingleRootFileSource(files.getFile());
			}
			else {
				throw new IllegalArgumentException("Unsupported resource type for file source: " + resource.getClass());
			}
		}
		return sources;
	}

	@Override
	public BinaryFile getBinaryFileNamed(String name) {
		for (FileSource resource : this.sources) {
			try {
				UrlResource uri = new UrlResource(resource.getUri());
				if (uri.exists()) {
					return resource.getBinaryFileNamed(name);
				}
			}
			catch (IOException e) {
				// Ignore
			}
		}
		throw new IllegalStateException("Cannot create file for " + name);
	}

	@Override public TextFile getTextFileNamed(String name) {
		for (FileSource resource : this.sources) {
			TextFile file = resource.getTextFileNamed(name);
			try {
				file.readContentsAsString();
				return file;
			} catch (RuntimeException e) {
				// Ignore
			}
		}
		return null;
	}

	@Override
	public void createIfNecessary() {
		throw new UnsupportedOperationException("Resource file sources are read-only");
	}

	@Override
	public FileSource child(String subDirectoryName) {
		List<FileSource> childSources = new ArrayList<>();
		for (FileSource resource : this.sources) {
			try {
				UrlResource uri = new UrlResource(resource.child(subDirectoryName).getUri());
				if (uri.createRelative(subDirectoryName).exists()) {
					childSources.add(resource.child(subDirectoryName));
				}
			}
			catch (IOException e) {
				// Ignore
			}
		}
		if (!childSources.isEmpty()) {
			return new ResourcesFileSource(childSources.toArray(new FileSource[0]));
		}
		return this.sources[0].child(subDirectoryName);
	}

	@Override
	public String getPath() {
		for (FileSource resource : this.sources) {
			try {
				UrlResource uri = new UrlResource(resource.getUri());
				if (uri.exists()) {
					return resource.getPath();
				}
			}
			catch (IOException e) {
				// Ignore
			}
		}
		return this.sources[0].getPath();
	}

	@Override
	public URI getUri() {
		for (FileSource resource : this.sources) {
			try {
				UrlResource uri = new UrlResource(resource.getUri());
				if (uri.exists()) {
					return resource.getUri();
				}
			}
			catch (IOException e) {
				// Ignore
			}
		}
		return this.sources[0].getUri();
	}

	@Override
	public List<TextFile> listFilesRecursively() {
		List<TextFile> files = new ArrayList<>();
		for (FileSource resource : this.sources) {
			files.addAll(resource.listFilesRecursively());
		}
		return files;
	}

	@Override
	public void writeTextFile(String name, String contents) {
		throw new UnsupportedOperationException("Resource file sources are read-only");
	}

	@Override
	public void writeBinaryFile(String name, byte[] contents) {
		throw new UnsupportedOperationException("Resource file sources are read-only");
	}

	@Override
	public boolean exists() {
		for (FileSource resource : this.sources) {
			if (resource.exists()) {
				return true;
			}
		}
		return false;
	}

	@Override public void deleteFile(String name) {

	}

}
