package io.codearte.accurest.wiremock

interface SingleFileConverter {

    boolean canHandleFileName(String fileName)

    String convertContent(String content)

    String getOutputFileNameForInputFileName(String inputFileName)
}