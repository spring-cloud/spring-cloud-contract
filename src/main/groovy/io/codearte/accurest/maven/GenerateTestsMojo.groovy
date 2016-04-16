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
import org.apache.maven.plugins.annotations.*
import org.apache.maven.project.MavenProject

import static java.lang.String.format

@Mojo(name = 'generateTests', defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES, requiresDependencyResolution = ResolutionScope.TEST)
@CompileStatic
class GenerateTestsMojo extends AbstractMojo {

    @Parameter(property = 'accurest.contractsDirectory', defaultValue = '${project.basedir}/src/test/accurest')
    private File contractsDirectory

    @Parameter(defaultValue = '${project.build.directory}/generated-test-sources/accurest')
    private File generatedTestSourcesDir

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

    @Component
    private MavenProject project

    @Parameter(property = 'accurest.skip', defaultValue = 'false')
    private boolean skip

    void execute() throws MojoExecutionException, MojoFailureException {

        if (skip) {
            log.info("Skipping accurest execution: accurest.skip=${skip}")
            return
        }

        log.info('Generating server tests source code for Accurest contract verification')

        AccurestConfigProperties config = new AccurestConfigProperties()
        config.contractsDslDir = contractsDirectory
        config.generatedTestSourcesDir = generatedTestSourcesDir
        config.targetFramework = testFramework
        config.testMode = testMode
        config.basePackageForTests = basePackageForTests
        config.baseClassForTests = baseClassForTests
        config.ruleClassForTests = ruleClassForTests
        config.nameSuffixForTests = nameSuffixForTests

        project.addTestCompileSourceRoot(generatedTestSourcesDir.absolutePath)

        if (log.isInfoEnabled()) {
            log.info("Test Source directory: $generatedTestSourcesDir added.");
            log.info("Using ${config.baseClassForTests} as base class for test classes")
        }

        try {
            TestGenerator generator = new TestGenerator(config)
            int generatedClasses = generator.generate()
            log.info("Generated $generatedClasses test classes.")
        } catch (AccurestException e) {
            throw new MojoExecutionException(format("Accurest Plugin exception: %s", e.message), e)
        }

    }

}
