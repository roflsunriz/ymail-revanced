package io.github.roflsunriz.ymail

import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.w3c.dom.Document
import org.w3c.dom.Element

class ManifestTransformsTest {
    @Test
    fun `removes ad permissions while preserving Firebase components required at startup`() {
        val document = parse(
            """
            <manifest xmlns:android="http://schemas.android.com/apk/res/android">
                <uses-permission android:name="com.google.android.gms.permission.AD_ID" />
                <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
                <application>
                    <provider android:name="com.google.android.gms.ads.MobileAdsInitProvider" />
                    <service android:name="com.google.firebase.messaging.FirebaseMessagingService" />
                    <service android:name="com.google.firebase.components.ComponentDiscoveryService">
                        <meta-data android:name="com.google.firebase.components:com.google.firebase.analytics.connector.internal.AnalyticsConnectorRegistrar" android:value="com.google.firebase.components.ComponentRegistrar" />
                        <meta-data android:name="com.google.firebase.components:com.google.firebase.crashlytics.CrashlyticsRegistrar" android:value="com.google.firebase.components.ComponentRegistrar" />
                        <meta-data android:name="com.google.firebase.components:com.google.firebase.messaging.FirebaseMessagingRegistrar" android:value="com.google.firebase.components.ComponentRegistrar" />
                    </service>
                </application>
            </manifest>
            """.trimIndent(),
        )

        document.removeAdvertisingSurfaces()

        val permissions = document.elements("uses-permission").map { it.getAttribute("android:name") }
        assertFalse("com.google.android.gms.permission.AD_ID" in permissions)
        assertTrue("android.permission.POST_NOTIFICATIONS" in permissions)

        val providers = document.elements("provider")
        assertEquals("false", providers.first { it.getAttribute("android:name").contains("MobileAds") }
            .getAttribute("android:enabled"))
        assertNotNull(providers.firstOrNull { it.getAttribute("android:name") == "app.revanced.extension.ymail.BootstrapProvider" })

        val metadata = document.elements("meta-data").map { it.getAttribute("android:name") }
        assertTrue(metadata.any { it.contains("AnalyticsConnectorRegistrar") })
        assertTrue(metadata.any { it.contains("CrashlyticsRegistrar") })
        assertTrue(metadata.any { it.contains("FirebaseMessagingRegistrar") })
        assertTrue(metadata.contains("firebase_analytics_collection_deactivated"))
    }

    private fun parse(xml: String): Document = DocumentBuilderFactory.newInstance()
        .newDocumentBuilder()
        .parse(ByteArrayInputStream(xml.toByteArray()))

    private fun Document.elements(tagName: String): List<Element> {
        val nodes = getElementsByTagName(tagName)
        return (0 until nodes.length).mapNotNull { nodes.item(it) as? Element }
    }
}
