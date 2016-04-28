package io.codearte.accurest.maven

import groovy.transform.CompileStatic
import org.apache.maven.model.Plugin
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
import org.codehaus.plexus.archiver.jar.Manifest

@Mojo(name = 'generateStubs', defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true)
@CompileStatic
class GenerateStubsMojo extends AbstractMojo {

    private static final String STUB_MAPPING_FILE_PATTERN = '**/*.json'
    private static final String ACCUREST_FILE_PATTERN = '**/*.groovy'

    @Parameter(defaultValue = '${project.build.directory}', readonly = true, required = true)
    private File projectBuildDirectory

    @Parameter(property = 'stubsDirectory', defaultValue = '${project.build.directory}/accurest')
    private File outputDirectory

    @Parameter(property = 'accurest.skip', defaultValue = 'false')
    private boolean skip

    @Component
    private MavenProjectHelper projectHelper;

    @Parameter(defaultValue = '${project}', readonly = true)
    private MavenProject project

    @Component(role = Archiver.class, hint = "jar")
    private JarArchiver archiver;

    @Parameter(defaultValue = 'true')
    private boolean attachContracts

    @Parameter(defaultValue = 'stubs')
    private String classifier

    void execute() throws MojoExecutionException, MojoFailureException {

        if (skip) {
            log.info("Skipping accurest execution: accurest.skip=${skip}")
            return
        }

        File stubsJarFile = createStubJar(outputDirectory)
        projectHelper.attachArtifact(project, 'jar', classifier, stubsJarFile)
    }

    private File createStubJar(File stubsOutputDir) {
        if (!stubsOutputDir.exists()) {
            throw new MojoExecutionException("Stubs could not be found: $stubsOutputDir.\nPlease make sure that accurest:convert was invoked");
        }
        String stubArchiveName = "${project.build.finalName}-${classifier}.jar";
        File stubsJarFile = new File(projectBuildDirectory, stubArchiveName);

        try {
            if (attachContracts) {
                archiver.addDirectory(stubsOutputDir, [STUB_MAPPING_FILE_PATTERN, ACCUREST_FILE_PATTERN] as String[], [] as String[]);
            } else {
                log.info("Skipping attaching accurest contracts")
                archiver.addDirectory(stubsOutputDir, [STUB_MAPPING_FILE_PATTERN] as String[], [ACCUREST_FILE_PATTERN] as String[]);
            }
            archiver.setCompress(true);
            archiver.setDestFile(stubsJarFile);
            archiver.addConfiguredManifest(createManifest());
            archiver.createArchive();
        } catch (Exception e) {
            throw new MojoFailureException("Exception while packaging ${classifier} jar.", e);
        }
        return stubsJarFile
    }

    private Manifest createManifest() {
        Manifest manifest = new Manifest();
        Plugin accurestMavenPlugin = project.getBuildPlugins().find { it.artifactId == 'accurest-maven-plugin' }
        manifest.addConfiguredAttribute(new Manifest.Attribute("Accurest-Maven-Plugin-Version", accurestMavenPlugin.version));
        if (accurestMavenPlugin.getDependencies()) {
            String accurestVersion = accurestMavenPlugin.getDependencies().find {
                it.artifactId == 'accurest-core'
            }.version
            manifest.addConfiguredAttribute(new Manifest.Attribute("Accurest-Version", accurestVersion));
        }
        return manifest
    }

}
