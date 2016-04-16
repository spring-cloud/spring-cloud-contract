package io.codearte.accurest.maven

import groovy.transform.CompileStatic
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Component
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import org.apache.maven.project.MavenProjectHelper
import org.codehaus.plexus.archiver.Archiver
import org.codehaus.plexus.archiver.jar.JarArchiver

@Mojo(name = 'generateStubs', defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true)
@CompileStatic
class GenerateStubsMojo extends AbstractMojo {

    @Parameter(defaultValue = '${project.build.directory}', readonly = true, required = true)
    private File projectBuildDirectory

    @Parameter(property = 'stubsDirectory', defaultValue = '${project.build.directory}/mappings')
    private File outputDirectory

    @Parameter(property = 'accurest.skip', defaultValue = 'false')
    private boolean skip

    @Component
    private MavenProjectHelper projectHelper;

    @Parameter(defaultValue = '${project}', readonly = true)
    private MavenProject project

    @Component(role = Archiver.class, hint = "jar")
    private JarArchiver jarArchiver;

    void execute() throws MojoExecutionException, MojoFailureException {

        if (skip) {
            log.info("Skipping accurest execution: accurest.skip=${skip}")
            return
        }

        File stubJarFile = createStubJar(outputDirectory)
        projectHelper.attachArtifact(project, 'jar', 'stubs', stubJarFile)
    }

    private File createStubJar(File stubsOutputDir) {
        if (!stubsOutputDir.exists()) {
            throw new MojoExecutionException("Stubs could not be found: $stubsOutputDir.\nPlease make sure that accurest:convert was invoked");
        }
        String stubArchiveName = project.getBuild().getFinalName() + "-stubs.jar";
        File stubJarFile = new File(projectBuildDirectory, stubArchiveName);

        try {
            jarArchiver.addDirectory(stubsOutputDir);
            jarArchiver.setCompress(true);
            jarArchiver.setDestFile(stubJarFile);
            jarArchiver.createArchive();
        } catch (Exception e) {
            throw new MojoFailureException('Exception while packaging.', e);
        }
        return stubJarFile
    }

}
