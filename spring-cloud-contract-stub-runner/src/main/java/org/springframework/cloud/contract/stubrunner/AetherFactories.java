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
import java.nio.file.Files;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import shaded.org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import shaded.org.apache.maven.settings.Settings;
import shaded.org.apache.maven.settings.building.DefaultSettingsBuilderFactory;
import shaded.org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import shaded.org.apache.maven.settings.building.SettingsBuilder;
import shaded.org.apache.maven.settings.building.SettingsBuildingException;
import shaded.org.apache.maven.settings.building.SettingsBuildingRequest;
import shaded.org.apache.maven.settings.building.SettingsBuildingResult;
import shaded.org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import shaded.org.eclipse.aether.impl.DefaultServiceLocator;
import shaded.org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import shaded.org.eclipse.aether.spi.connector.transport.TransporterFactory;
import shaded.org.eclipse.aether.transport.file.FileTransporterFactory;
import shaded.org.eclipse.aether.transport.http.HttpTransporterFactory;

import org.springframework.util.StringUtils;

final class AetherFactories {

	private static final Log log = LogFactory.getLog(AetherFactories.class);

	private static final String MAVEN_LOCAL_REPOSITORY_LOCATION = "maven.repo.local";

	private static final String MAVEN_USER_SETTINGS_LOCATION = "org.apache.maven.user-settings";

	private static final String MAVEN_GLOBAL_SETTINGS_LOCATION = "org.apache.maven.global-settings";

	private static final Random RANDOM = new Random();

	private AetherFactories() {
		throw new IllegalStateException("Can't instantiate a utility class");
	}

	public static RepositorySystem newRepositorySystem() {
		DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
		locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
		locator.addService(TransporterFactory.class, FileTransporterFactory.class);
		locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
		return locator.getService(RepositorySystem.class);
	}

	public static RepositorySystemSession newSession(RepositorySystem system, boolean workOffline) {
		DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
		session.setOffline(workOffline);
		if (!workOffline) {
			session.setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_ALWAYS);
		}
		session.setChecksumPolicy(RepositoryPolicy.CHECKSUM_POLICY_WARN);
		String localRepositoryDirectory = localRepositoryDirectory(workOffline);
		if (log.isDebugEnabled()) {
			log.debug("Local Repository Directory set to [" + localRepositoryDirectory + "]. Work offline: ["
					+ workOffline + "]");
		}
		LocalRepository localRepo = new LocalRepository(localRepositoryDirectory);
		session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
		return session;
	}

	protected static String localRepositoryDirectory(boolean workOffline) {
		String localRepoLocationFromSettings = settings().getLocalRepository();
		String currentLocalRepo = readPropertyFromSystemProps(localRepoLocationFromSettings);
		if (workOffline) {
			return currentLocalRepo;
		}
		return temporaryDirectory();
	}

	private static String temporaryDirectory() {
		try {
			return Files.createTempDirectory("aether-local").toString();
		}
		catch (IOException e) {
			if (log.isDebugEnabled()) {
				log.debug("Failed to create a new temporary directory, will generate a new one under temp dir");
			}
			return System.getProperty("java.io.tmpdir") + File.separator + RANDOM.nextInt();
		}
	}

	private static String readPropertyFromSystemProps(String localRepoLocationFromSettings) {
		String mavenLocalRepo = fromSystemPropOrEnv(MAVEN_LOCAL_REPOSITORY_LOCATION);
		return StringUtils.hasText(mavenLocalRepo) ? mavenLocalRepo
				: localRepoLocationFromSettings != null ? localRepoLocationFromSettings
						: System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository";
	}

	// system prop takes precedence over env var
	private static String fromSystemPropOrEnv(String prop) {
		String resolvedProp = System.getProperty(prop);
		if (StringUtils.hasText(resolvedProp)) {
			return resolvedProp;
		}
		return System.getenv(prop);
	}

	private static File userSettings() {
		String user = fromSystemPropOrEnv(MAVEN_USER_SETTINGS_LOCATION);
		if (user == null) {
			File file = new File(new File(System.getProperty("user.home")).getAbsoluteFile(),
					File.separator + ".m2" + File.separator + "settings.xml");
			if (log.isDebugEnabled()) {
				log.debug("No custom maven user settings provided, will use [" + file + "]");
			}
			return file;
		}
		if (log.isDebugEnabled()) {
			log.debug("Custom location provided for user settings [" + user + "]");
		}
		return new File(user);
	}

	protected static Settings settings() {
		SettingsBuilder builder = new DefaultSettingsBuilderFactory().newInstance();
		SettingsBuildingRequest request = new DefaultSettingsBuildingRequest();
		request.setUserSettingsFile(userSettings());
		String global = fromSystemPropOrEnv(MAVEN_GLOBAL_SETTINGS_LOCATION);
		if (global != null) {
			request.setGlobalSettingsFile(new File(global));
		}
		SettingsBuildingResult result;
		try {
			result = builder.build(request);
		}
		catch (SettingsBuildingException ex) {
			throw new IllegalStateException(ex);
		}
		return result.getEffectiveSettings();
	}

}
