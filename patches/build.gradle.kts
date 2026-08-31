group = "io.github.roflsunriz.ymail"

patches {
    about {
        name = "Yahoo!メール ReVanced Patches"
        description = "Yahoo!メールの広告通信・広告枠・セルフプロモーションを除去するパッチ"
        source = "https://github.com/roflsunriz/ymail-revanced"
        author = "roflsunriz"
        contact = "https://github.com/roflsunriz/ymail-revanced/issues"
        website = "https://github.com/roflsunriz/ymail-revanced"
        license = "MIT License"
    }
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
