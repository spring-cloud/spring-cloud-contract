package io.codearte.accurest.wiremock

import io.coderate.accurest.dsl.GroovyDsl

abstract class DslToWiremockConverter implements SingleFileConverter {

    @Override
    boolean canHandleFileName(String fileName) {
        return fileName.endsWith('.groovy')
    }

    @Override
    String getOutputFileNameForInputFileName(String inputFileName) {
        return inputFileName.replaceAll('.groovy', '.json')
    }

    protected GroovyDsl createGroovyDSLfromStringContent(String groovyDslAsString) {
        return (GroovyDsl)new GroovyShell(this.class.classLoader).evaluate("$groovyDslAsString")
    }
}
