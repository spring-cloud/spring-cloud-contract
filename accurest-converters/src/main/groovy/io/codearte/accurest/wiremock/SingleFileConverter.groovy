package io.codearte.accurest.wiremock

import groovy.transform.CompileStatic

@CompileStatic
interface SingleFileConverter {

    boolean canHandleFileName(String fileName)

    String convertContent(String content)

    String generateOutputFileNameForInput(String inputFileName)
}