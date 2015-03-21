Accurate REST
=============
Consumer Driven Contracts verifier for Java

# Introduction

We would like to use the Wiremock's JSON stub definitions as a point of entry to our approach of Consumer Driven Contracts (CDC). From these JSON stub definitions we would like to generate Acceptance tests that will allow you to start using CDC as TDD from architecture point of view.

# Notations

  * __Collaborator__ - a service whom your service can contact
  * __Client test__ - since your service is a client of your collaborator then the tests of your service are __client tests__
  * __Server test__ - from a your service's (client) perspective your collaborator is a server that provides a functionality thus tests of your collaborators are __server tests__

# Why?

The main purpose of this approach is to:

  - ensure that our stubs are doing exactly what the actual implementation does
  - generate acceptance test cases from stub definitions (ATDD)
  - make the stub definitions reusable

Below we depict the idea behind client and server side testing. Let's assume that for the sake
of this description that there is a _service X_ calling a _service Y_.

## Client side (service X)

During the tests you want to have a Wiremock instance up and running that simulates the service Y.
You would like to feed that instance with a proper stub definition. That stub definition would need
to be valid from the Wiremock's perspective but should also be reusable on the server side.

__Summing it up:__ On this side, in the stub definition, you can use patterns for request stubbing and you need exact
values for responses.

## Server side (service Y)

Being a service Y since you are developing your stub, you need to be sure that it's actually resembling your
concrete implementation. You can't have a situation where your stub acts in one way and your application on
production behaves in a different way.

That's why from the provided stub acceptance tests will be generated that will ensure
that your application behaves in the same way as you define in your stub.

__Summing it up:__ On this side, in the stub definition, you need exact values as request and can use patterns/methods
for response verification.

# Description

To achieve that we needed to tweak the standard Wiremock stub definitions by providing a possibility of entering
two values for one field. This is done via the following pattern:

```
${VALUE_FOR_CLIENT_TESTS:VALUE_FOR_SERVER_TESTS}
```

That means that depending on the need you can take either value for the client test or server test.

## Example

Let's take a look at the following example

```
{
    "request": {
        "method": "GET",
        "urlPattern": "${/[0-9]{2}:/12}"
    },
    "response": {
        "status": 200,
        "body": "{\"date\":\"${\"2015-01-14\":$anyInt($it)}\"}}",
        "headers": {
            "Content-Type": "text/plain"
        }
    }
}
```

### Client side

From the client's perspective, in this particular scenario, you want a regexp matching a URL on which you send
a request (`[/0-9]{2}`) and a concrete value returned (`2015-01-14`). That way we will change your stub definition to:

```
{
    "request": {
        "method": "GET",
        "urlPattern": "/[0-9]{2}"
    },
    "response": {
        "status": 200,
        "body": {
            "date" : "2015-01-14"
        },
        "headers": {
            "Content-Type": "text/plain"
        }
    }
}
```

### Server side

On the server side we need to generate acceptance tests. So we need to check that for given input (request)
we receive some matching output (response).

In this case we want to send a request to an endpoint `/12` and check if in the body of the response
we will receive the current date (`isCurrentDate(it.date)`).

The latter value will be checked if it's a method (if it contains parentheses) and then a method will be
called with the corresponding field's (status, body, headers etc.) value as input.

`VALUE_FOR_SERVER_TESTS` can use `$it` notation to pass body as method argument. The method in the generated Spec can be access also with `$` prefix - if you want to call a method `isPersonalIdValid(String requestBody)` you can do it as follows:

```
personaId : "${123456789:$isPersonalIdValid($it)}"
```

#### Example of generated specification

```
def responseBody = new JsonSlurper().parseText(response.body.asString())
isPersonalIdValid(responseBody.personaId)

```

### Full example

Below you can see sample stub and code of the test generated for this definition.

Stub definition:

```
{
  "request": {
    "method": "POST",
    "url": "/loanApplication",
    "headers": {
      "Content-Type": {"equalTo": "application/vnd.loanapplicationservice.v1+json"}
    },
    "bodyPatterns": [{"matches": "\\{\"clientSsn\":\"1234567890\",\"loanAmount\":123.123\\}"}]
  },
  "response": {
    "status": 200,
    "body": "{\"loanApplicationStatus\":\"LOAN_APPLIED\",\"loanApplicationId\":\"${3245:$greaterThan($it, 1000)}\"}",
    "headers": {"Content-Type": "application/vnd.loanapplicationservice.v1+json"}
  }
}
```

and the code:

```
class AcceptanceSpec extends AssurestSpec {

	def shouldApplyForLoan() {
		given:
			def request = given()
					.header('Content-Type', 'application/vnd.loanapplicationservice.v1+json')
					.body('{"clientSsn":"1234567890","loanAmount":123.123}')

		when:
			def response = given().spec(request)
					.post("/loanApplication")

		then:
			response.statusCode == 200
			response.header('Content-Type') == 'application/vnd.loanapplicationservice.v1+json'
			def responseBody = new JsonSlurper().parseText(response.body.asString())
			greaterThan(responseBody.loanApplicationId, 1000)
			responseBody.loanApplicationStatus == "LOAN_APPLIED"
	}

}

```

# Using in your project

## Add gradle plugin

```
buildscript {
	repositories {
		mavenCentral()
	}
	dependencies {
		classpath 'io.codearte.accurest:accurest-gradle-plugin:0.4.4'
	}
}

apply plugin: 'accurest'

dependecies {
	testCompile 'org.spockframework:spock-core:0.7-groovy-2.0'
	testCompile 'com.jayway.restassured:rest-assured:2.4.0'
	testCompile 'com.jayway.restassured:spring-mock-mvc:2.4.0' // needed if you're going to use Spring MockMvc
}
```

## Add stubs

By default Accurest is looking for stubs in src/test/resources/stubs directory.
Directory containing stub definitions is treated as a class name, and each stub definition is treated as a single test.
We assume that it contains at least one directory which will be used as test class name. If there is more than one level of nested directories all except the last one will be used as package name.
So with following structure

src/test/resources/stubs/myservice/shouldCreateUser.json
src/test/resources/stubs/myservice/shouldReturnUser.json

Accurest will create test class `defaultBasePackage.MyService` with two methods
 - shouldCreateUser()
 - shouldReturnUser()

## Run plugin

Plugin registers itself to be invoked before `compileTestGroovy` task. You have nothing to do as long as you want it to be part of your build process. If you just want to generate tests please invoke `generateAccurest` task.

## Configure plugin

To change default configuration just add `accurest` snippet to your Gradle config

```
accurest {
	testMode = 'MockMvc'
	baseClassForTests = 'org.mycompany.tests'
	generatedTestSourcesDir = 'src/accurest'
}
```

### Configuration options

 - testMode - default 'MockMvc' uses Spring MockMvc to invoke tests. Can be changed to 'Direct' to support HTTP requests
 - stubsBaseDirectory - where to look for stub definitions. Default 'src/test/resources/stubs'
 - basePackageForTests - base package for test classes. Default 'io.codearte.accurest.tests'
 - baseClassForTests - base class which will be extended by all generated tests. By default Accurest is using base framework class (for Spock it's `Specification`)
 - ruleClassForTests - you can specify qualified name of rule which should be included in generated test
 - generatedTestSourcesDir - target directory for generated tests. By default 'build/generated-sources/accurest'
 - imports - array with imports that should be included in generated tests (for example ['org.myorg.Matchers']). By default empty array []
 - staticImports - array with static imports that should be included in generated tests(for example ['org.myorg.Matchers.*']). By default empty array []
