package io.codearte.accurest.maven

import groovy.transform.CompileStatic
import io.codearte.accurest.stubrunner.StubConfiguration
import io.codearte.accurest.stubrunner.StubRunnerOptions

@CompileStatic
public class EnhancedStubRunnerOptions {

    @Delegate private final StubRunnerOptions options
    private final Collection<StubConfiguration> dependencies

    EnhancedStubRunnerOptions(Collection<StubConfiguration> dependencies, StubRunnerOptions options) {
        this.options = options
        this.dependencies = dependencies
    }

    EnhancedStubRunnerOptions(Integer minPortValue, Integer maxPortValue, String stubRepositoryRoot, boolean workOffline, String stubsClassifier, Collection<StubConfiguration> dependencies, StubRunnerOptions options) {
        this.options = new StubRunnerOptions(minPortValue, maxPortValue, stubRepositoryRoot, workOffline, stubsClassifier)
        this.dependencies = dependencies
    }

    Collection<StubConfiguration> getDependencies() {
        return dependencies
    }

    StubRunnerOptions getStubsRunnerOptions() {
        return options
    }
}
