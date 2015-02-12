package io.coderate.accurest.dsl.internal

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TypeChecked

@TypeChecked
@EqualsAndHashCode(includeFields = true)
@ToString(includePackage = false)
class WithValuePattern {

    DslProperty<String> equalTo
    DslProperty<String> equalToJson
    DslProperty<String> equalToXml
    DslProperty<String> matchesXPath
    DslProperty<JSONCompareMode> jsonCompareMode
    DslProperty<String> contains
    DslProperty<String> matches
    DslProperty<String> doesNotMatch
    DslProperty<String> absent
    DslProperty<String> matchesJsonPath

    void equalTo(String equalTo) {
        this.equalTo = new DslProperty<String>(equalTo)
    }

    void equalTo(DslProperty equalTo) {
        this.equalTo = equalTo
    }

    void equalToJson(String equalToJson) {
        this.equalToJson = new DslProperty<String>(equalToJson)
    }

    void equalToJson(DslProperty equalToJson) {
        this.equalToJson = equalToJson
    }

    void equalToXml(String equalToXml) {
        this.equalToXml = new DslProperty<String>(equalToXml)
    }

    void equalToXml(DslProperty equalToXml) {
        this.equalToXml = equalToXml
    }

    void matchesXPath(String matchesXPath) {
        this.matchesXPath = new DslProperty<String>(matchesXPath)
    }

    void matchesXPath(DslProperty matchesXPath) {
        this.matchesXPath = matchesXPath
    }

    void jsonCompareMode(JSONCompareMode jsonCompareMode) {
        this.jsonCompareMode = new DslProperty<JSONCompareMode>(jsonCompareMode)
    }

    void jsonCompareMode(DslProperty jsonCompareMode) {
        this.jsonCompareMode = jsonCompareMode
    }

    void contains(String contains) {
        this.contains = new DslProperty<String>(contains)
    }

    void contains(DslProperty contains) {
        this.contains = contains
    }

    void matches(String matches) {
        this.matches = new DslProperty<String>(matches)
    }

    void matches(DslProperty matches) {
        this.matches = matches
    }

    void doesNotMatch(String doesNotMatch) {
        this.doesNotMatch = new DslProperty<String>(doesNotMatch)
    }

    void doesNotMatch(DslProperty doesNotMatch) {
        this.doesNotMatch = doesNotMatch
    }

    void absent(String absent) {
        this.absent = new DslProperty<String>(absent)
    }

    void absent(DslProperty absent) {
        this.absent = absent
    }

    void matchesJsonPath(String matchesJsonPath) {
        this.matchesJsonPath = new DslProperty<String>(matchesJsonPath)
    }

    void matchesJsonPath(DslProperty matchesJsonPath) {
        this.matchesJsonPath = matchesJsonPath
    }
}
