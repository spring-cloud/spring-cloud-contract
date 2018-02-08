package contracts

listOf(
        org.springframework.cloud.contract.spec.KotlinContract.make {
            request {
                name("should count all frauds")
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
        org.springframework.cloud.contract.spec.KotlinContract.make {
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