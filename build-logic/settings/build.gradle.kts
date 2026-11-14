plugins {
    `kotlin-dsl`
}

group = "com.jorgelillo.buildlogic"

gradlePlugin {
    plugins {
        register("securityFloors") {
            id = "jorgelillo.security-floors"
            implementationClass = "SecurityFloorsSettingsPlugin"
        }
    }
}
