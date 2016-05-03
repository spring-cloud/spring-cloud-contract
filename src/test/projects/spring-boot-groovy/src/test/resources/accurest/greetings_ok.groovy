io.codearte.accurest.dsl.GroovyDsl.make {
    priority 2
    request {
        method 'GET'
        urlPath('/greeting') {
            queryParameters {
                parameter 'name': 'Something'
            }
        }
        headers {
            header 'Content-Type': 'application/json'
        }
    }
    response {
        status 200
        body (
                id: 1,
                content: "Hello, Something!"
        )
    }
}