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
	
	static String buildRootPath(Project project) {
		String groupId = project.group as String
		String artifactId = project.name
		String version = project.version
		return "META-INF/${groupId}/${artifactId}/${version}"
	}
}
