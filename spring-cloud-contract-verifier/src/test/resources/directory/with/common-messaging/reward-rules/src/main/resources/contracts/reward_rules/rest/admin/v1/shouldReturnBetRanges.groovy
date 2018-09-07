import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description('Should return bet ranges array')
    request {
        method 'GET'
        url('/admin/v1/spin/betRanges')
    }

    response {
        status 200
        body(
                betRanges: [
                        [
                                betRangeId    : 3,
                                fromBetPercent: -1
                        ],
                        [
                                betRangeId    : 4,
                                fromBetPercent: 0
                        ],
                        [
                                betRangeId    : 1,
                                fromBetPercent: 90
                        ],
                        [
                                betRangeId    : 2,
                                fromBetPercent: 130
                        ]
            ]
        )

        headers {
            contentType(applicationJsonUtf8())
        }
    }
}

