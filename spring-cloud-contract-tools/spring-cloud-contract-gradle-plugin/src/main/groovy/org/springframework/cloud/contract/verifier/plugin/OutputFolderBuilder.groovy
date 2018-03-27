package org.springframework.cloud.contract.verifier.plugin

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.Project

/**
 * @author Marcin Grzejszczak
 */
@CompileStatic
@PackageScope
class OutputFolderBuilder {

	private static final String DEFAULT_MAPPINGS_FOLDER = 'mappings'
	
	static String buildRootPath(Project project) {
		String groupId = project.group as String
		String artifactId = project.name
		String version = project.version
		return "META-INF/${groupId}/${artifactId}/${version}"
	}
	
	static File outputMappingsDir(Project project, File stubsOutputDir) {
		String root = OutputFolderBuilder.buildRootPath(project)
		return stubsOutputDir != null ?
				new File(stubsOutputDir, "${root}/${DEFAULT_MAPPINGS_FOLDER}")
				: new File(project.buildDir, "stubs/${root}/${DEFAULT_MAPPINGS_FOLDER}")
	}
}
