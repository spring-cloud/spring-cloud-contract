io.coderate.accurest.dsl.GroovyDsl.make {
    request {
        method 'PUT'
        url '/api/12'
        headers {
            header 'Content-Type': 'application/json'
        }
        body '''\
    [{
        "text": "Gonna see you at Warsaw"
    }]
'''
    }
    response {
        status 200
    }
}