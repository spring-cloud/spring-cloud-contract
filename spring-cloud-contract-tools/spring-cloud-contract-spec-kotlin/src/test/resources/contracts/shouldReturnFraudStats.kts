package contracts

import org.springframework.cloud.contract.spec.KContract

arrayOf(
        KContract.make {
            request {
                method("GET")
                url("/frauds")
            }
            response {
                status(200)
                body(mapOf("count" to 200))
                headers {
                    contentType("application/vnd.fraud.v1+json")
                }
            }
        },
        KContract.make {
            request {
                method("GET")
                url("/drunks")
            }
            response {
                status(200)
                body(mapOf("count" to 100))
                headers {
                    contentType("application/vnd.fraud.v1+json")
                }
            }
        }
)