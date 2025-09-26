/*
 * Copyright 2013-present the original author or authors.
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
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;

import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * Utilities for creating/obtaining Aether (Maven Resolver) components.
 *
 * <p>
 * <b>Key changes vs the old version:</b>
 * <ul>
 * <li>No hard reference to {@code BasicRepositoryConnectorFactory} or transporter
 * implementations. We register them reflectively if present, otherwise we rely on Maven's
 * injected components or whatever the runtime provides via ServiceLoader.</li>
 * <li>Supports using Maven-injected {@link RepositorySystem} and
 * {@link RepositorySystemSession} when called from a Mojo, avoiding classpath issues
 * entirely.</li>
 * </ul>
 */
final class AetherFactories {

	private static final Log log = LogFactory.getLog(AetherFactories.class);

	private static final String MAVEN_LOCAL_REPOSITORY_LOCATION = "maven.repo.local";

	private static final String MAVEN_USER_SETTINGS_LOCATION = "org.apache.maven.user-settings";

	private static final String MAVEN_GLOBAL_SETTINGS_LOCATION = "org.apache.maven.global-settings";

	private static final Random RANDOM = new Random();

	private AetherFactories() {
		throw new IllegalStateException("Can't instantiate a utility class");
	}

	/**
	 * Return the injected system if available, otherwise create a new one via a
	 * {@link DefaultServiceLocator} without hard-linking to optional providers.
	 */
	static RepositorySystem repositorySystemOr(@Nullable RepositorySystem injectedOrNull) {
		if (injectedOrNull != null) {
			if (log.isDebugEnabled()) {
				log.debug("Using Maven-injected RepositorySystem");
			}
			return injectedOrNull;
		}
		if (log.isDebugEnabled()) {
			log.debug("No injected RepositorySystem provided; creating one via ServiceLocator");
		}
		return newRepositorySystemFallback();
	}

	/**
	 * Return the injected session if available, otherwise create a new session for the
	 * given system.
	 */
	static RepositorySystemSession sessionOr(RepositorySystem system, @Nullable RepositorySystemSession injectedOrNull,
			boolean workOffline) {
		if (injectedOrNull != null) {
			if (log.isDebugEnabled()) {
				log.debug("Using Maven-injected RepositorySystemSession (workOffline=" + injectedOrNull.isOffline()
						+ ")");
			}
			return injectedOrNull;
		}
		return newSession(system, workOffline);
	}

	/**
	 * Fallback creation using a ServiceLocator. This tries to register common providers
	 * reflectively:
	 * <ul>
	 * <li>org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory</li>
	 * <li>org.eclipse.aether.transport.file.FileTransporterFactory</li>
	 * <li>org.eclipse.aether.transport.http.HttpTransporterFactory</li>
	 * </ul>
	 * If any of these are missing on the classpath, we simply don't register them and let
	 * ServiceLoader discover whatever is available. This avoids
	 * {@code NoClassDefFoundError} at class load time.
	 */
	private static RepositorySystem newRepositorySystemFallback() {
		DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();

		// Helpful diagnostics
		locator.setErrorHandler(new DefaultServiceLocator.ErrorHandler() {
			@Override
			public void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable exception) {
				if (log.isDebugEnabled()) {
					log.debug("Failed to create service " + type.getName() + " via "
							+ (impl != null ? impl.getName() : "<null>") + ": " + exception.toString());
				}
			}
		});

		// Try to register connector + transporters reflectively, but do not hard-link
		// them.
		registerIfPresent(locator, "org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory",
				RepositoryConnectorFactory.class);

		registerIfPresent(locator, "org.eclipse.aether.transport.file.FileTransporterFactory",
				TransporterFactory.class);

		registerIfPresent(locator, "org.eclipse.aether.transport.http.HttpTransporterFanewRepositorySystemctory",
				TransporterFactory.class);

		RepositorySystem system = locator.getService(RepositorySystem.class);

		// If system still ended up null, give a helpful hint.
		if (system == null) {
			throw new IllegalStateException("Failed to obtain RepositorySystem. "
					+ "Ensure Maven Resolver is on the classpath and, when running inside a Maven plugin, "
					+ "prefer using the Maven-injected RepositorySystem/RepositorySystemSession.");
		}
		return system;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void registerIfPresent(DefaultServiceLocator locator, String implClassName, Class<?> serviceType) {
		try {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			if (cl == null) {
				cl = AetherFactories.class.getClassLoader();
			}
			Class<?> impl = Class.forName(implClassName, false, cl);
			if (!serviceType.isAssignableFrom(impl)) {
				if (log.isDebugEnabled()) {
					log.debug("Class " + implClassName + " is not assignable to " + serviceType.getName());
				}
				return;
			}
			locator.addService((Class) serviceType, (Class) impl);
			if (log.isDebugEnabled()) {
				log.debug("Registered " + implClassName + " as " + serviceType.getSimpleName());
			}
		}
		catch (ClassNotFoundException ex) {
			// Silently skip; not on classpath (e.g., plugin didn't ship connector-basic).
			if (log.isDebugEnabled()) {
				log.debug("Optional provider not found on classpath: " + implClassName);
			}
		}
	}

	/**
	 * Create a new {@link RepositorySystemSession}, controlling offline/update/checksum
	 * policies.
	 */
	static RepositorySystemSession newSession(RepositorySystem system, boolean workOffline) {
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

	/**
	 * Determine local repo directory: respect settings/system prop; use temp when online
	 * to avoid pollution.
	 */
	static String localRepositoryDirectory(boolean workOffline) {
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
				log.debug("Failed to create a new temporary directory, will generate a new one under temp dir", e);
			}
			return System.getProperty("java.io.tmpdir") + File.separator + RANDOM.nextInt();
		}
	}

	private static String readPropertyFromSystemProps(@Nullable String localRepoLocationFromSettings) {
		String mavenLocalRepo = fromSystemPropOrEnv(MAVEN_LOCAL_REPOSITORY_LOCATION);
		return StringUtils.hasText(mavenLocalRepo) ? mavenLocalRepo
				: (localRepoLocationFromSettings != null ? localRepoLocationFromSettings
						: System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository");
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

	static Settings settings() {
		SettingsBuilder builder = new DefaultSettingsBuilderFactory().newInstance();
		SettingsBuildingRequest request = new DefaultSettingsBuildingRequest();
		request.setUserSettingsFile(userSettings());
		String global = fromSystemPropOrEnv(MAVEN_GLOBAL_SETTINGS_LOCATION);
		if (global != null) {
			request.setGlobalSettingsFile(new File(global));
		}
		try {
			SettingsBuildingResult result = builder.build(request);
			return result.getEffectiveSettings();
		}
		catch (SettingsBuildingException ex) {
			throw new IllegalStateException(ex);
		}
	}

}
