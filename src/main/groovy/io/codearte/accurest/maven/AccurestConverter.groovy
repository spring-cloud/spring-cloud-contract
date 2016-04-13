package io.codearte.accurest.maven;

import groovy.transform.CompileStatic;
import groovy.transform.PackageScope;
import io.codearte.accurest.config.AccurestConfigProperties;
import io.codearte.accurest.wiremock.DslToWireMockClientConverter;
import io.codearte.accurest.wiremock.RecursiveFilesConverter;

@CompileStatic
class AccurestConverter {

    static void convertAccurestToStubs(AccurestConfigProperties config) {
        RecursiveFilesConverter converter = new RecursiveFilesConverter(new DslToWireMockClientConverter(), config)
        converter.processFiles()
    }

}
