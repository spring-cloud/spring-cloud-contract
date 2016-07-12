org.springframework.cloud.contract.spec.Contract.make {
    request {
        method 'POST'
        url('/users') {

        }
        headers {
            header 'Content-Type': 'application/json'
        }
        body '''{ "login" : "john", "name": "John The Contract" }'''
    }
    response {
        status 200
        headers {
            header 'Location': '/users/john'
        }
    }
}