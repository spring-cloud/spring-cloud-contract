/*
 *  Copyright 2013-2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.stubrunner;

import java.io.File;

import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.springframework.util.StringUtils;
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

class AetherFactories {

	private static final String MAVEN_LOCAL_REPOSITORY_LOCATION = "maven.repo.local";
	private static final String MAVEN_USER_SETTINGS_LOCATION = "org.apache.maven.user-settings";
	private static final String MAVEN_GLOBAL_SETTINGS_LOCATION = "org.apache.maven.global-settings";

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
		LocalRepository localRepo = new LocalRepository(localRepositoryDirectory());
		session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
		return session;
	}

	private static String localRepositoryDirectory() {
		String localRepoLocationFromSettings = settings().getLocalRepository();
		return readPropertyFromSystemProps(localRepoLocationFromSettings);
	}

	private static String readPropertyFromSystemProps(
			String localRepoLocationFromSettings) {
		String mavenLocalRepo = fromSystemPropOrEnv(MAVEN_LOCAL_REPOSITORY_LOCATION);
		return StringUtils.hasText(mavenLocalRepo) ? mavenLocalRepo :
				localRepoLocationFromSettings != null ? localRepoLocationFromSettings
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

	private static Settings settings() {
		SettingsBuilder builder = new DefaultSettingsBuilderFactory().newInstance();
		SettingsBuildingRequest request = new DefaultSettingsBuildingRequest();
		String user = fromSystemPropOrEnv(MAVEN_USER_SETTINGS_LOCATION);
		if (user == null) {
			request.setUserSettingsFile(new File(new File(System.getProperty("user.home")).getAbsoluteFile(),
					File.separator + ".m2" + File.separator + "settings.xml"));
		} else {
			request.setUserSettingsFile(new File(user));
		}
		String global = fromSystemPropOrEnv(MAVEN_GLOBAL_SETTINGS_LOCATION);
		if (global != null) {
			request.setGlobalSettingsFile(new File(global));
		}
		SettingsBuildingResult result;
		try {
			result = builder.build(request);
		} catch (SettingsBuildingException ex) {
			throw new IllegalStateException(ex);
		}
		return result.getEffectiveSettings();
	}

}
