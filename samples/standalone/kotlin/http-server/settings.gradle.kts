pluginManagement {
    repositories {
        mavenCentral()
        mavenLocal()
        maven { url = uri("https://repo.spring.io/release") }
        maven { url = uri("https://repo.spring.io/milestone") }
        maven { url = uri("https://repo.spring.io/snapshot") }
        gradlePluginPortal()
    }
    resolutionStrategy {
        val bootVersion: String by settings
        val verifierVersion: String by settings
        eachPlugin {
            if (requested.id.id == "org.springframework.boot") {
                useModule("org.springframework.boot:spring-boot-gradle-plugin:$bootVersion")
            }
            if (requested.id.id == "spring-cloud-contract") {
                useModule("org.springframework.cloud:spring-cloud-contract-gradle-plugin:$verifierVersion")
            }
        }
    }
}
rootProject.name = "http-server-kotlin-gradle"
