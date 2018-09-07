import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("""
Should return empty array if user has no friends
""")
    request {
        method 'GET'
        urlPath('/SocialServer/social/getFriends')
        url('/social/getFriends') {
            queryParameters {
                parameter 'snId': $(consumer(regex('([^\\W]|-)+')), producer('12345'))
                parameter 'snType': $(consumer(regex('\\d+')), producer(2))
            }
        }
    }

    response {
        status 200
        body(
            """
            {"friends" : []}
        """)

        headers {
            contentType(applicationJsonUtf8())
        }
    }
}

