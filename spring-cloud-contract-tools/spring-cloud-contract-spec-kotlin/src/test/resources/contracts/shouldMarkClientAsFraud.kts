package contracts

import org.springframework.cloud.contract.spec.contract

contract {
    request { // (1)
        method("PUT") // (2)
        url("/fraudcheck") // (3)
        body( // (4)
                "clientId" to dynamic(consumer = regex("[0-9]{10}"), producer = "8532032713"),
                "loanAmount" to 99999
        )
        headers { // (5)
            contentType("application/vnd.fraud.v1+json")
        }
    }
    response { // (6)
        status(200) // (7)
        body( // (8)
                "fraudCheckStatus" to "FRAUD",
                "rejectionReason" to "Amount too high"
        )
        headers { // (9)
            contentType("application/vnd.fraud.v1+json")
        }
    }
}

/*
Since we don't want to force on the user to hardcode values of fields that are dynamic
(timestamps, database ids etc.), one can parametrize those entries. If you wrap your field's
 value in a `$(...)` or `value(...)` and provide a dynamic value of a field then
 the concrete value will be generated for you. If you want to be really explicit about
 which side gets which value you can do that by using the `value(consumer(...), producer(...))` notation.
 That way what's present in the `consumer` section will end up in the produced stub. What's
 there in the `producer` will end up in the autogenerated test. If you provide only the
 regular expression side without the concrete value then Spring Cloud Contract will generate one for you.

From the Consumer perspective, when shooting a request in the integration test:

(1) - If the consumer sends a request
(2) - With the "PUT" method
(3) - to the URL "/fraudcheck"
(4) - with the JSON body that
 * has a field `clientId` that matches a regular expression `[0-9]{10}`
 * has a field `loanAmount` that is equal to `99999`
(5) - with header `Content-Type` equal to `application/vnd.fraud.v1+json`
(6) - then the response will be sent with
(7) - status equal `200`
(8) - and JSON body equal to
 { "fraudCheckStatus": "FRAUD", "rejectionReason": "Amount too high" }
(9) - with header `Content-Type` equal to `application/vnd.fraud.v1+json`

From the Producer perspective, in the autogenerated producer-side test:

(1) - A request will be sent to the producer
(2) - With the "PUT" method
(3) - to the URL "/fraudcheck"
(4) - with the JSON body that
 * has a field `clientId` that will have a generated value that matches a regular expression `[0-9]{10}`
 * has a field `loanAmount` that is equal to `99999`
(5) - with header `Content-Type` equal to `application/vnd.fraud.v1+json`
(6) - then the test will assert if the response has been sent with
(7) - status equal `200`
(8) - and JSON body equal to
 { "fraudCheckStatus": "FRAUD", "rejectionReason": "Amount too high" }
(9) - with header `Content-Type` matching `application/vnd.fraud.v1+json.*`
 */