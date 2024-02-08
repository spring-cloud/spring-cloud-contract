/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package contracts

// tag::class[]
import org.springframework.cloud.contract.spec.ContractDsl.Companion.contract

contract {
    request {
        method = PUT
        url = url("/multipart")
        multipart {
            field("formParameter", value(consumer(regex("\".+\"")), producer("\"formParameterValue\"")))
            field("someBooleanParameter", value(consumer(anyBoolean), producer("true")))
            field("file",
                named(
                    // name of the file
                    value(consumer(regex(nonEmpty)), producer("filename.csv")),
                    // content of the file
                    value(consumer(regex(nonEmpty)), producer("file content")),
                    // content type for the part
                    value(consumer(regex(nonEmpty)), producer("application/json"))
                )
            )
        }
        headers {
            contentType = "multipart/form-data;boundary=AaB03x"
        }
    }
    response {
        status = OK
    }
}
// end::class[]
