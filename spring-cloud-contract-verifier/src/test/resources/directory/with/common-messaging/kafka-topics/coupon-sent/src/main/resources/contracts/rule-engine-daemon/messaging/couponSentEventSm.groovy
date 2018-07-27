import org.springframework.cloud.contract.spec.Contract

Contract.make {

    description("""Should send a message in topic coupon_sent""")

    label 'couponSentSm'

    input {
        triggeredBy('couponSentSm()')
    }

    outputMessage {
        sentTo('coupon_sent')

        body([
                senderUserId: value(consumer(123), producer(regex('\\d+'))),
                sessionId: value(consumer(7928568413097907541), producer(regex('\\d+'))),
                createdTs: value(consumer(1504688949158), producer(regex('\\d+'))),
                couponToken: value(consumer("440006-6-1504688949139-xyuzzrx5"), producer(regex('([^\\W]|-)+')))
        ])

        headers {
            messagingContentType(applicationJsonUtf8())
        }
    }
}