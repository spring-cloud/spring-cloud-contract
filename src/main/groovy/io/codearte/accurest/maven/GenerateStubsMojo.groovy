package io.codearte.accurest.maven

import groovy.transform.CompileStatic
import org.apache.maven.model.path.PathTranslator
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo

import javax.inject.Inject

@Mojo(name = 'generateStubs', defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
@CompileStatic
@Deprecated
class GenerateStubsMojo extends ConvertMojo {

    @Inject
    GenerateStubsMojo(PathTranslator translator) {
        super(translator)
    }
}
