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
package org.springframework.cloud.contract.verifier.maven.stubrunner

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.maven.project.MavenProject
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.RepositorySystemSession
import org.springframework.cloud.contract.stubrunner.AetherStubDownloader

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Named
@Singleton
@CompileStatic
@Slf4j
class AetherStubDownloaderFactory {

    private final MavenProject project
    private final RepositorySystem repoSystem

    @Inject
    AetherStubDownloaderFactory(RepositorySystem repoSystem, MavenProject project) {
        this.repoSystem = repoSystem
        this.project = project
    }

    AetherStubDownloader build(RepositorySystemSession repoSession) {
        return new AetherStubDownloader(repoSystem, project.remoteProjectRepositories, repoSession)
    }

}
