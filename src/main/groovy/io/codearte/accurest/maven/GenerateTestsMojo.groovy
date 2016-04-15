package io.codearte.accurest.maven

import groovy.transform.CompileStatic
import io.codearte.accurest.AccurestException
import io.codearte.accurest.TestGenerator
import io.codearte.accurest.config.AccurestConfigProperties
import io.codearte.accurest.config.TestFramework
import io.codearte.accurest.config.TestMode
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter

import static java.lang.String.format

@Mojo(name = 'generateTests', defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES)
@CompileStatic
class GenerateTestsMojo extends AbstractMojo {

    @Parameter(defaultValue = '${basedir}', readonly = true, required = true)
    private File baseDir

    @Parameter(defaultValue = '${project.build.directory}', readonly = true, required = true)
    private File projectBuildDirectory

    @Parameter(defaultValue = '/src/test/accurest')
    private String contractsDir

    @Parameter(defaultValue = '/generated-test-sources/accurest')
    private String generatedTestSourcesDir

    @Parameter(defaultValue = 'io.codearte.accurest.tests')
    private String basePackageForTests

    @Parameter
    private String baseClassForTests

    @Parameter(defaultValue = 'MOCKMVC')
    private TestMode testMode

    @Parameter(defaultValue = 'JUNIT')
    private TestFramework testFramework

    @Parameter
    private String ruleClassForTests

    @Parameter
    private String nameSuffixForTests

    void execute() throws MojoExecutionException, MojoFailureException {
        log.info('Generating server tests source code for Accurest contract verification')

        AccurestConfigProperties config = new AccurestConfigProperties()
        config.contractsDslDir = new File(baseDir, contractsDir)
        config.generatedTestSourcesDir = new File(projectBuildDirectory, generatedTestSourcesDir)
        config.targetFramework = testFramework
        config.testMode = testMode
        config.basePackageForTests = basePackageForTests
        config.baseClassForTests = baseClassForTests
        config.ruleClassForTests = ruleClassForTests
        config.nameSuffixForTests = nameSuffixForTests

        log.info("Using ${config.generatedTestSourcesDir} as test source directory")
        log.info("Using ${config.baseClassForTests} as base class for test classes")

        try {
            TestGenerator generator = new TestGenerator(config)
            int generatedClasses = generator.generate()
            log.info("Generated $generatedClasses test classes.")
        } catch (AccurestException e) {
            throw new MojoExecutionException(format("Accurest Plugin exception: %s", e.getMessage()), e)
        }
    }

}
