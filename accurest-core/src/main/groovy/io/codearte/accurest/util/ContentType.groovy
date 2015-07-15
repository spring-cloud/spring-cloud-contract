package io.codearte.accurest.util

enum ContentType {

    JSON("application/json"),
    XML("application/xml"),
    UNKNOWN("application/octet-stream")

    final String mimeType

    ContentType(String mimeType) {
        this.mimeType = mimeType
    }

}