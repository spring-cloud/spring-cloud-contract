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
package org.springframework.cloud.contract.maven.verifier;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.springframework.cloud.contract.maven.verifier.ManifestCreator;

@Mojo(name = "generateStubs", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true)
public class GenerateStubsMojo extends AbstractMojo {

    private static final String STUB_MAPPING_FILE_PATTERN = "**/*.json";
    private static final String CONTRACT_FILE_PATTERN = "**/*.groovy";

    @Parameter(defaultValue = "${project.build.directory}", readonly = true, required = true)
    private File projectBuildDirectory;

    @Parameter(property = "stubsDirectory", defaultValue = "${project.build.directory}/stubs")
    private File outputDirectory;

    /**
     * Set this to "true" to bypass Verifier execution..
     */
    @Parameter(property = "spring.cloud.contract.verifier.skip", defaultValue = "false")
    private boolean skip;

    @Component
    private MavenProjectHelper projectHelper;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Component(role = Archiver.class, hint = "jar")
    private JarArchiver archiver;

    @Parameter(defaultValue = "true")
    private boolean attachContracts;

    @Parameter(defaultValue = "stubs")
    private String classifier;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Skipping Spring Cloud Contract Verifier execution: spring.cloud.contract.verifier.skip=" + skip);
            return;
        }
        File stubsJarFile = createStubJar(outputDirectory);
        projectHelper.attachArtifact(project, "jar", classifier, stubsJarFile);
    }

    private File createStubJar(File stubsOutputDir) throws MojoFailureException, MojoExecutionException {
        if (!stubsOutputDir.exists()) {
            throw new MojoExecutionException("Stubs could not be found: [" + stubsOutputDir.getAbsolutePath() + "] .\nPlease make sure that spring-cloud-contract:convert was invoked");
        }
        String stubArchiveName = project.getBuild().getFinalName() + "-" + classifier + ".jar";
        File stubsJarFile = new File(projectBuildDirectory, stubArchiveName);
        try {
            if (attachContracts) {
                archiver.addDirectory(stubsOutputDir, new String[]{STUB_MAPPING_FILE_PATTERN,
                        CONTRACT_FILE_PATTERN }, new String[0]);
            } else {
                getLog().info("Skipping attaching Spring Cloud Contract Verifier contracts");
                archiver.addDirectory(stubsOutputDir, new String[]{STUB_MAPPING_FILE_PATTERN}, new String[]{
                        CONTRACT_FILE_PATTERN });
            }
            archiver.setCompress(true);
            archiver.setDestFile(stubsJarFile);
            archiver.addConfiguredManifest(ManifestCreator.createManifest(project));
            archiver.createArchive();
        } catch (Exception e) {
            throw new MojoFailureException("Exception while packaging " + classifier + " jar.", e);
        }
        return stubsJarFile;
    }

}
