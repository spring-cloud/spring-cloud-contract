package org.springframework.cloud.contract.stubrunner;

import java.io.File;
import java.lang.reflect.Field;

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
		SettingsDecrypter settingsDecrypter = new DefaultSettingsDecrypter();
		setField(DefaultSettingsDecrypter.class, "securityDispatcher", settingsDecrypter,
				new MavenSettings.SpringCloudContractSecDispatcher());
		return settingsDecrypter;
	}

	private void setField(Class<?> sourceClass, String fieldName, Object target,
						  Object value) {
		try {
			Field field = sourceClass.getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(target, value);
		} catch (Exception ex) {
			throw new IllegalStateException(
					"Failed to set field '" + fieldName + "' on '" + target + "'", ex);
		}
	}

	private class SpringCloudContractSecDispatcher extends DefaultSecDispatcher {

		private static final String SECURITY_XML = "settings-security.xml";

		SpringCloudContractSecDispatcher() {
			File file = new File(MavenSettings.this.homeDir, SECURITY_XML);
			this._configurationFile = file.getAbsolutePath();
			try {
				this._cipher = new DefaultPlexusCipher();
			} catch (PlexusCipherException e) {
				throw new IllegalStateException(e);
			}
		}

	}
}
