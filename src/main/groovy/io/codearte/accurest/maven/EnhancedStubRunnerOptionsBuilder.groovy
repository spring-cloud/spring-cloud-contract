package io.codearte.accurest.maven

import groovy.transform.CompileStatic
import io.codearte.accurest.stubrunner.StubConfiguration
import io.codearte.accurest.stubrunner.StubRunnerOptions
import io.codearte.accurest.stubrunner.util.StubsParser

@CompileStatic
public class EnhancedStubRunnerOptionsBuilder {

    private LinkedList<String> stubs = new LinkedList<>()

    private final StubRunnerOptions options;

    EnhancedStubRunnerOptionsBuilder(StubRunnerOptions options) {
        this.options = options
    }

    EnhancedStubRunnerOptionsBuilder(Integer minPortValue, Integer maxPortValue, String stubRepositoryRoot,
                                     boolean workOffline, String stubsClassifier) {
        this.options = new StubRunnerOptions(minPortValue, maxPortValue, stubRepositoryRoot, workOffline, stubsClassifier)
    }

    EnhancedStubRunnerOptionsBuilder withStubs(String stubs) {
        addStub(stubsToList(stubs))
        return this
    }

    private Collection<StubConfiguration> buildDependencies() {
        return StubsParser.fromString(stubs, options.getStubsClassifier());
    }

    private static List<String> stubsToList(String stubIdsToPortMapping) {
        return stubIdsToPortMapping.split(',').collect { it }
    }

    private void addStub(List<String> notations) {
        for (String notation : notations) {
            addStub(notation);
        }
    }

    private void addStub(String notation) {
        if (StubsParser.hasPort(notation)) {
            addPort(notation);
            stubs.add(StubsParser.ivyFromStringWithPort(notation));
        } else {
            stubs.add(notation);
        }
    }

    private void addPort(String notation) {
        options.putStubIdsToPortMapping(StubsParser.fromStringWithPort(notation));
    }

    EnhancedStubRunnerOptions build() {
        return new EnhancedStubRunnerOptions(buildDependencies(), options)
    }
}
