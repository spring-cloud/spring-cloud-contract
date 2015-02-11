package io.coderate.accurest.dsl.internal

import static io.coderate.accurest.dsl.internal.DelegateHelper.delegateToClosure

class WithValuePattern {

    SingleTypeCustomizableProperty<String> equalToJson
    SingleTypeCustomizableProperty<String> equalToXml
    StringCustomizableProperty matchesXPath
    SingleTypeCustomizableProperty<JSONCompareMode> jsonCompareMode
    SingleTypeCustomizableProperty<String> equalTo
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
        this.equalTo = new SingleTypeCustomizableProperty(equalTo)
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
