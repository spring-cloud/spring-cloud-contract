/**
 *
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
package org.springframework.cloud.contract.maven.verifier

import org.apache.maven.model.Plugin
import org.apache.maven.project.MavenProject
import org.codehaus.plexus.archiver.jar.Manifest

class ManifestCreator {
    static Manifest createManifest(MavenProject project) {
        Manifest manifest = new Manifest();
        Plugin verifierMavenPlugin = project.getBuildPlugins().find { it.artifactId == 'spring-cloud-contract-maven-plugin' }
        manifest.addConfiguredAttribute(new Manifest.Attribute("Spring-Cloud-Contract-Verifier-Maven-Plugin-Version", verifierMavenPlugin.version));
        if (verifierMavenPlugin.getDependencies()) {
            String verifierVersion = verifierMavenPlugin.getDependencies().find {
                it.artifactId == 'spring-cloud-contract-verifier-core'
            }.version
            manifest.addConfiguredAttribute(new Manifest.Attribute("Spring-Cloud-Contract-Verifier-Version", verifierVersion));
        }
        return manifest
    }
}
