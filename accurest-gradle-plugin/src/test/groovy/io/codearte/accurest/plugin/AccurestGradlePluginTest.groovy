package io.codearte.accurest.plugin

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Ignore
import spock.lang.Specification

/**
 * @author Jakub Kubrynski
 */
@Ignore
class AccurestGradlePluginTest extends Specification {

	def void greeterPluginAddsGreetingTaskToProject() {
		when:
			Project project = ProjectBuilder.builder().build()
			project.apply plugin: 'accurest'

		then:
			project.tasks.generateAccurest
	}
}
