/*
 * Copyright 2013-2019 the original author or authors.
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

import org.springframework.cloud.contract.spec.ContractDsl.Companion.contract

contract {
    request {
        method = POST
        url = url("/tests")
        multipart {
            field("file1", named(
                    value(consumer(regex(nonEmpty)), producer("filename1")),
                    value(consumer(regex(nonEmpty)), producer("content1"))))
            field("file2", named(
                    value(consumer(regex(nonEmpty)), producer("filename2")),
                    value(consumer(regex(nonEmpty)), producer("content2"))))
            field("test", named(
                    value(consumer(regex(nonEmpty)), producer("filename3")),
                    value(consumer(regex(nonEmpty)), producer(file("test.json"))),
                    value("application/json")))
        }
        headers {
            contentType = "multipart/form-data"
        }
    }
    response {
        status = OK
        headers {
            contentType = "application/json"
        }
    }
}
