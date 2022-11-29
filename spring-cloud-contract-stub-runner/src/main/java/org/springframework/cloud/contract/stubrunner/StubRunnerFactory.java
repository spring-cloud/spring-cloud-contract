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

package org.springframework.cloud.contract.stubrunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.contract.stubrunner.provider.wiremock.WireMockHttpServerStub;
import org.springframework.cloud.contract.verifier.converter.RecursiveFilesConverter;
import org.springframework.cloud.contract.verifier.converter.StubGenerator;
import org.springframework.cloud.contract.verifier.converter.StubGeneratorProvider;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifierSender;
import org.springframework.cloud.contract.verifier.wiremock.DslToWireMockClientConverter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.SpringFactoriesLoader;

/**
 * Factory of StubRunners. Basing on the options and passed collaborators downloads the
 * stubs and returns a list of corresponding stub runners.
 */
class StubRunnerFactory {

	private static final Log log = LogFactory.getLog(StubRunnerFactory.class);

	private final StubRunnerOptions stubRunnerOptions;

	private final StubDownloader stubDownloader;

	private final MessageVerifierSender<?> contractVerifierMessaging;

	StubRunnerFactory(StubRunnerOptions stubRunnerOptions, StubDownloader stubDownloader,
			MessageVerifierSender<?> contractVerifierMessaging) {
		this.stubRunnerOptions = stubRunnerOptions;
		this.stubDownloader = stubDownloader;
		this.contractVerifierMessaging = contractVerifierMessaging;
	}

	public Collection<StubRunner> createStubsFromServiceConfiguration() {
		if (log.isDebugEnabled()) {
			log.debug("Will download stubs for dependencies " + this.stubRunnerOptions.getDependencies());
		}
		if (this.stubRunnerOptions.getDependencies().isEmpty()) {
			log.warn("No stubs to download have been passed. Most likely you have forgotten to pass "
					+ "them either via annotation or a property");
		}
		Collection<StubRunner> result = new ArrayList<>();
		for (StubConfiguration stubsConfiguration : this.stubRunnerOptions.getDependencies()) {
			Map.Entry<StubConfiguration, File> entry = this.stubDownloader.downloadAndUnpackStubJar(stubsConfiguration);
			if (log.isDebugEnabled()) {
				log.debug(
						"For stub configuration [" + stubsConfiguration + "] the downloaded entry is [" + entry + "]");
			}
			if (entry != null) {
				Path path = resolvePath(entry.getValue());
				File unpackedLocation = path.toFile();
				if (this.stubRunnerOptions.isGenerateStubs()) {
					if (log.isDebugEnabled()) {
						log.debug(
								"Flag to generate stubs at runtime was switched on. Will remove the current mappings and will generate new ones.");
					}
					generateMappingsAtRuntime(path);
				}
				result.add(createStubRunner(entry.getKey(), unpackedLocation));
			}
		}
		return result;
	}

	private void generateMappingsAtRuntime(Path path) {
		removeCurrentMappings(path);
		generateNewMappings(path);
	}

	private Path resolvePath(File unpackedLocation) {
		Resource resource = ResourceResolver.resource(unpackedLocation.toURI().toString());
		Path path = unpackedLocation.toPath();
		if (resource != null) {
			try {
				return Paths.get(resource.getURI());
			}
			catch (IOException ex) {
				return unpackedLocation.toPath();
			}
		}
		return path;
	}

	private void removeCurrentMappings(Path path) {

		List<HttpServerStub> httpServerStubs = SpringFactoriesLoader.loadFactories(HttpServerStub.class, null);
		if (httpServerStubs.isEmpty()) {
			httpServerStubs.add(new WireMockHttpServerStub());
		}

		try {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

				private final Log log = LogFactory.getLog(StubRunnerFactory.class);

				private final StubGeneratorProvider provider = new StubGeneratorProvider();

				private final HttpServerStub wireMockHttpServerStub = new WireMockHttpServerStub();

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					File potentialStubMapping = file.toFile();
					Collection<StubGenerator> stubGenerators = this.provider
							.allOrDefault(new DslToWireMockClientConverter());
					if (stubGenerators.stream().anyMatch(s -> s.canReadStubMapping(potentialStubMapping))) {
						if (log.isDebugEnabled()) {
							log.debug("Deleting file [" + file.toString() + "] since it contains a valid mapping.");
						}
						try {
							Files.delete(file);
						}
						catch (IOException ex) {
							log.warn("Failed to delete file [" + file.toString() + "]", ex);
						}
					}
					return FileVisitResult.CONTINUE;
				}
			});
		}
		catch (IOException ex) {
			log.warn("Exception occurred while trying to delete mappings", ex);
		}
	}

	private void generateNewMappings(Path path) {
		File unpackedLocation = path.toFile();
		RecursiveFilesConverter converter = new RecursiveFilesConverter(
				subfolderIfPresent(unpackedLocation, "mappings"), subfolderIfPresent(unpackedLocation, "contracts"),
				new ArrayList<>(), ".*", false);
		converter.processFiles();
	}

	private File subfolderIfPresent(File unpackedLocation, String subfolder) {
		File subfolderDir = new File(unpackedLocation, subfolder);
		if (subfolderDir.exists()) {
			return subfolderDir;
		}
		return unpackedLocation;
	}

	private StubRunner createStubRunner(StubConfiguration stubsConfiguration, File unzipedStubDir) {
		if (unzipedStubDir == null) {
			return null;
		}
		return createStubRunner(unzipedStubDir, stubsConfiguration, this.stubRunnerOptions);
	}

	private StubRunner createStubRunner(File unzippedStubsDir, StubConfiguration stubsConfiguration,
			StubRunnerOptions stubRunnerOptions) {
		return new StubRunner(stubRunnerOptions, unzippedStubsDir.getPath(), stubsConfiguration,
				this.contractVerifierMessaging);
	}

}
