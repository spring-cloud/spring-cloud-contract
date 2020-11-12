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

import org.sonatype.plexus.components.cipher.DefaultPlexusCipher;
import org.sonatype.plexus.components.cipher.PlexusCipherException;
import org.sonatype.plexus.components.sec.dispatcher.DefaultSecDispatcher;
import shaded.org.apache.maven.settings.crypto.DefaultSettingsDecrypter;
import shaded.org.apache.maven.settings.crypto.SettingsDecrypter;

import org.springframework.util.StringUtils;

public class MavenSettings {

	private static final String MAVEN_USER_CONFIG_DIRECTORY = "maven.user.config.dir";

	private final String homeDir;

	public MavenSettings() {
		this(userSettings());
	}

	private static String fromSystemPropOrEnv(String prop) {
		String resolvedProp = System.getProperty(prop);
		if (StringUtils.hasText(resolvedProp)) {
			return resolvedProp;
		}
		return System.getenv(prop);
	}

	private static String userSettings() {
		String user = fromSystemPropOrEnv(MAVEN_USER_CONFIG_DIRECTORY);
		if (user == null) {
			return System.getProperty("user.home");
		}
		return user;
	}

	public MavenSettings(String homeDir) {
		this.homeDir = homeDir;
	}

	public SettingsDecrypter createSettingsDecrypter() {
		return new DefaultSettingsDecrypter(new SpringCloudContractSecDispatcher());
	}

	private class SpringCloudContractSecDispatcher extends DefaultSecDispatcher {

		private static final String SECURITY_XML = "settings-security.xml";

		SpringCloudContractSecDispatcher() {
			File file = new File(MavenSettings.this.homeDir, SECURITY_XML);
			this._configurationFile = file.getAbsolutePath();
			try {
				this._cipher = new DefaultPlexusCipher();
			}
			catch (PlexusCipherException e) {
				throw new IllegalStateException(e);
			}
		}

	}

}
