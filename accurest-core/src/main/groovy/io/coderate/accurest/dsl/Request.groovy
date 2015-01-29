package io.coderate.accurest.dsl

import groovy.transform.TypeChecked

@TypeChecked
class Request {
    String method
    String url
    StringCustomizableProperty urlPattern
    String urlPath
    Headers headers

    void method(String method) {
        this.method = method
    }

    void url(String url) {
        this.url = url
    }

    void urlPattern(@DelegatesTo(StringCustomizableProperty) Closure closure) {
        StringCustomizableProperty urlPattern = new StringCustomizableProperty()
        this.urlPattern = urlPattern
        delegateToClosure(closure, urlPattern)
    }

    void headers(@DelegatesTo(Headers) Closure closure) {
        Headers headers = new Headers()
        this.headers = headers
        delegateToClosure(closure, headers)
    }

    void urlPath(String urlPath) {
        this.urlPath = urlPath
    }

    //TODO: Can we have different types for Client/Server (client: pattern(String), server: value(Boolean/Int)) ?
    class CustomizableProperty<T, V> {
        private T client
        private V server

        void client(T client) {
            this.client = client
        }

        void server(V server) {
            this.server = server
        }

        T toClientSide() {
            return client
        }

        V toServerSide() {
            return server
        }
    }

    private class StringCustomizableProperty extends CustomizableProperty<String, String> {
        @Override
        String toClientSide() {
            return super.toClientSide() as String
        }

        @Override
        String toServerSide() {
            return super.toServerSide() as String
        }
    }

    private class NoOpCustomizableProperty<T> extends CustomizableProperty<T, T> {
        NoOpCustomizableProperty(T value) {
            client(value)
            server(value)
        }

        @Override
        public String toString() {
            return toClientSide()
        }
    }

    class Headers {
        private Map<String, WithValuePattern> headers = [:]

        WithValuePattern header(String headerName) {
            WithValuePattern withValuePattern = new WithValuePattern()
            headers[headerName] = withValuePattern
            return withValuePattern
        }

        Set<Map.Entry<String, WithValuePattern>> entries() {
            return headers.entrySet()
        }
    }

/**
 *
 * From Wiremock
 private Map<String, ValuePattern> queryParamPatterns;
 private List<ValuePattern> bodyPatterns;

 private String equalToJson;
 private String equalToXml;
 private String matchesXPath;
 private JSONCompareMode jsonCompareMode;
 private String equalTo;
 private String contains;
 private String matches;
 private String doesNotMatch;
 private Boolean absent;
 private String matchesJsonPath;

 */
    class WithValuePattern {
        NoOpCustomizableProperty<String> equalToJson
        NoOpCustomizableProperty<String> equalToXml
        StringCustomizableProperty matchesXPath
        NoOpCustomizableProperty<JSONCompareMode> jsonCompareMode
        NoOpCustomizableProperty<String> equalTo
        StringCustomizableProperty contains
        StringCustomizableProperty matches
        StringCustomizableProperty doesNotMatch
        CustomizableProperty<Boolean, Boolean> absent
        StringCustomizableProperty matchesJsonPath

//        void equalToJson(String equalToJson) {
//            this.equalToJson = equalToJson
//        }
//
        void equalTo(String equalTo) {
            this.equalTo = new NoOpCustomizableProperty(equalTo)
        }

//        void equalToXml(String equalToXml) {
//            this.equalToXml = equalToXml
//        }

        void matches(@DelegatesTo(StringCustomizableProperty) Closure closure) {
            StringCustomizableProperty placeholderHaving = new StringCustomizableProperty()
            this.matches = placeholderHaving
            delegateToClosure(closure, placeholderHaving)
        }

        void doesNotMatch(@DelegatesTo(StringCustomizableProperty) Closure closure) {
            StringCustomizableProperty placeholderHaving = new StringCustomizableProperty()
            this.doesNotMatch = placeholderHaving
            delegateToClosure(closure, placeholderHaving)
        }

        void contains(@DelegatesTo(StringCustomizableProperty) Closure closure) {
            StringCustomizableProperty placeholderHaving = new StringCustomizableProperty()
            this.contains = placeholderHaving
            delegateToClosure(closure, placeholderHaving)
        }

//        void jsonCompareMode(JSONCompareMode jsonCompareMode) {
//            this.jsonCompareMode = jsonCompareMode
//        }

    }

    enum JSONCompareMode {
        STRICT, LENIENT, NON_EXTENSIBLE, STRICT_ORDER;
    }

    private static <T> void delegateToClosure(@DelegatesTo(T) Closure closure, T delegate) {
        closure.delegate = delegate
        closure()
    }

}