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

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.DefaultSettingsBuilderFactory;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuilder;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingResult;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;

class AetherFactories {

	private static final String MAVEN_LOCAL_REPOSITORY_LOCATION = "maven.repo.local";

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
		return System.getProperty(MAVEN_LOCAL_REPOSITORY_LOCATION, localRepoLocationFromSettings != null
			? localRepoLocationFromSettings : System.getProperty("user.home") + "/.m2/repository");
	}

	private static Settings settings() {
		final SettingsBuilder builder = new DefaultSettingsBuilderFactory().newInstance();
		final SettingsBuildingRequest request = new DefaultSettingsBuildingRequest();
		final String user = System.getProperty("org.apache.maven.user-settings");
		if (user == null) {
			request.setUserSettingsFile(new File(new File(System.getProperty("user.home")).getAbsoluteFile(),
					"/.m2/settings.xml"
				));
		} else {
			request.setUserSettingsFile(new File(user));
		}
		final String global = System.getProperty("org.apache.maven.global-settings");
		if (global != null) {
			request.setGlobalSettingsFile(new File(global));
		}
		final SettingsBuildingResult result;
		try {
			result = builder.build(request);
		} catch (final SettingsBuildingException ex) {
			throw new IllegalStateException(ex);
		}
		return result.getEffectiveSettings();
	}

}
