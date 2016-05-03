package io.codearte.accurest.maven

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
        log.info('Copying accurest contracts')
        Resource testResource = new Resource(directory: contractsDirectory.absolutePath)
        MavenResourcesExecution mavenResourcesExecution = new MavenResourcesExecution(
                resources: [testResource],
                outputDirectory: new File(outputDirectory, 'accurest'),
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
