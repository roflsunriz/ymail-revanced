extension {
    name = "extensions/ymail.rve"
}

android {
    namespace = "app.revanced.extension.ymail"

    defaultConfig {
        minSdk = 26
    }
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
