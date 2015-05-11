io.codearte.accurest.dsl.GroovyDsl.make {
    request {
        method 'PUT'
        url $(client(regex('^/api/[0-9]{2}$')), server('/api/12'))
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
        body (
             path: $(client('/api/12'), server(regex('^/api/[0-9]{2}$')))
        )
        status 200
    }
}