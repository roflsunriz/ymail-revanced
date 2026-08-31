plugins {
    base
}

allprojects {
    dependencyLocking {
        lockAllConfigurations()
    }

    configurations.configureEach {
        resolutionStrategy.eachDependency {
            val securedVersion = when {
                requested.group == "io.netty" -> "4.1.137.Final"
                requested.group == "org.bouncycastle" -> "1.84"
                requested.group == "com.google.protobuf" -> "3.25.5"
                requested.group == "org.apache.commons" && requested.name == "commons-lang3" -> "3.20.0"
                requested.group == "org.apache.httpcomponents" && requested.name == "httpclient" -> "4.5.13"
                else -> null
            }
            if (securedVersion != null) {
                useVersion(securedVersion)
                because("既知脆弱性を含まない監査済みバージョンへ統一するため")
            }
        }
    }
}
