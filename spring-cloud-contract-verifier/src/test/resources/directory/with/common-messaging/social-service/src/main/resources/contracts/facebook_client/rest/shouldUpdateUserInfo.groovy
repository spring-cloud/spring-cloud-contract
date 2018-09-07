import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("""
User's information should be update if has appropriate age
""")
    request {
        method 'POST'
        url '/test/updateUserInfo'
        body([
            userId: 123,
            age: 25,
            firstName: "asd",
            lastName: "asd"
        ])
        stubMatchers {
            jsonPath('$.userId', byRegex("[1-9]{1}([0-9]{7})"))
            jsonPath('$.age', byRegex("(1[89]|[2-9][0-9])"))
            jsonPath('$.firstName', byRegex("[a-zA-Z]{2,20}"))
            jsonPath('$.lastName', byRegex("[a-zA-Z]{2,20}"))
        }
        headers {
            contentType(applicationJson())
        }
    }
    response {
        status 200
        body([
            userId: fromRequest().body("userId"),
            age: fromRequest().body("age"),
            firstName: fromRequest().body("firstName"),
            lastName: fromRequest().body("lastName")
        ])
        testMatchers {
            jsonPath('$.userId', byEquality())
            jsonPath('$.age', byEquality())
            jsonPath('$.firstName', byEquality())
            jsonPath('$.lastName', byEquality())
        }
        headers {
            contentType(applicationJson())
        }
    }
}