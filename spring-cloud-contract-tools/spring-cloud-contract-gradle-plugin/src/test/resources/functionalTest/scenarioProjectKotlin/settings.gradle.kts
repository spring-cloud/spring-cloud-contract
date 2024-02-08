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

pluginManagement {
    repositories {
        mavenCentral()
        mavenLocal()
        maven(url = "https://repo.spring.io/snapshot")
        maven(url = "https://repo.spring.io/milestone")
        maven(url = "https://repo.spring.io/release")
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "org.springframework.boot") {
                val bootVersion: String by extra
                useModule("org.springframework.boot:spring-boot-gradle-plugin:$bootVersion")
            } else if (requested.id.id == "org.springframework.cloud.contract") {
                val verifierVersion: String by extra
                useModule("org.springframework.cloud:spring-cloud-contract-gradle-plugin:$verifierVersion")
            }
        }
    }
}

include(":fraudDetectionService")
include(":loanApplicationService")
