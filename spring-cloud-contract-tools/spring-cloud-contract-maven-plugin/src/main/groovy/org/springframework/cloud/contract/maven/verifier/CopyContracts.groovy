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

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.maven.execution.MavenSession
import org.apache.maven.model.Resource
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.project.MavenProject
import org.apache.maven.shared.filtering.MavenFilteringException
import org.apache.maven.shared.filtering.MavenResourcesExecution
import org.apache.maven.shared.filtering.MavenResourcesFiltering

@CompileStatic
@Slf4j
class CopyContracts {

    private final MavenProject project
    private final MavenSession mavenSession
    private final MavenResourcesFiltering mavenResourcesFiltering

    CopyContracts(MavenProject project, MavenSession mavenSession, MavenResourcesFiltering mavenResourcesFiltering) {
        this.project = project
        this.mavenSession = mavenSession
        this.mavenResourcesFiltering = mavenResourcesFiltering
    }

    void copy(File contractsDirectory, File outputDirectory) {
        log.info('Copying Spring Cloud Contract Verifier contracts')
        Resource testResource = new Resource(directory: contractsDirectory.absolutePath)
        MavenResourcesExecution mavenResourcesExecution = new MavenResourcesExecution(
                resources: [testResource],
                outputDirectory: new File(outputDirectory, 'contracts'),
                mavenProject: project,
                encoding: 'UTF-8',
                mavenSession: mavenSession);
        mavenResourcesExecution.injectProjectBuildFilters = false
        mavenResourcesExecution.overwrite = true
        mavenResourcesExecution.includeEmptyDirs = false
        mavenResourcesExecution.filterFilenames = false
        try {
            mavenResourcesFiltering.filterResources(mavenResourcesExecution);
        } catch (MavenFilteringException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

}
