io.codearte.accurest.dsl.GroovyDsl.make {
    priority 2
    request {
        method 'GET'
        url('/greeting')
        headers {
            header 'Content-Type': 'application/json'
        }
    }
    response {
        status 200
        body(
                id: 1,
                content: "Hello, World!"
        )
    }
}