/*
 *  Copyright 2013-2016 the original author or authors.
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
package org.springframework.cloud.contract.maven.verifier;

import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.jar.Manifest;
import org.codehaus.plexus.archiver.jar.ManifestException;

class ManifestCreator {
	public static Manifest createManifest(MavenProject project) throws ManifestException {
		Manifest manifest = new Manifest();
		Plugin verifierMavenPlugin = findMavenPlugin(project.getBuildPlugins());
		if (verifierMavenPlugin != null) {
			manifest.addConfiguredAttribute(new Manifest.Attribute(
					"Spring-Cloud-Contract-Verifier-Maven-Plugin-Version", verifierMavenPlugin.getVersion()));
		}
		if (verifierMavenPlugin != null && !verifierMavenPlugin.getDependencies().isEmpty()) {
			Dependency verifierDependency = findVerifierDependency(verifierMavenPlugin.getDependencies());
			if (verifierDependency != null) {
				String verifierVersion = verifierDependency.getVersion();
				manifest.addConfiguredAttribute(new Manifest.Attribute("Spring-Cloud-Contract-Verifier-Version",
						verifierVersion));
			}
		}
		return manifest;
	}

	private static Plugin findMavenPlugin(List<Plugin> plugins) {
		for (Plugin plugin : plugins) {
			if ("spring-cloud-contract-maven-plugin".equals(plugin.getArtifactId())) {
				return plugin;
			}
		}
		return null;
	}

	private static Dependency findVerifierDependency(List<Dependency> deps) {
		for (Dependency dep : deps) {
			if ("spring-cloud-contract-verifier".equals(dep.getArtifactId())) {
				return dep;
			}
		}
		return null;
	}

}
