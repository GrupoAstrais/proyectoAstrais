plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
}

group = "com.astrais"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(21)
}

// TODO: Poner los strings de implementacion en libs.version.toml y usar la manera sin string.
dependencies {
    implementation(libs.ktor.server.core)

    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.apache)
    implementation(libs.ktor.server.host.common)
    implementation(libs.kotlin.asyncapi.ktor)
    implementation(libs.ktor.network.tls)

    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.status.pages)

    // Serializacion por JSON
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    implementation(libs.ktor.server.sse)

    // Exposed
    val exposed_ver = "1.0.0"
    implementation("org.jetbrains.exposed:exposed-core:$exposed_ver")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_ver")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_ver")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposed_ver")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")

    // Prueba de postgres
    implementation("org.postgresql:postgresql:42.7.7")

    // BCrypt (Hash seguro para las contraseñas) https://github.com/patrickfav/bcrypt
    // Comparaciones de velocidad: https://github.com/patrickfav/bcrypt?tab=readme-ov-file#performance
    implementation("at.favre.lib:bcrypt:0.10.2")

    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)


    testImplementation(libs.ktor.server.test.host)
}
