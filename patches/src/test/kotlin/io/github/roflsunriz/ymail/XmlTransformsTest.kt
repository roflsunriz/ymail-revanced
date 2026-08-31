package io.github.roflsunriz.ymail

import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.w3c.dom.Document
import org.w3c.dom.Element

class XmlTransformsTest {
    @Test
    fun `collapses data binding ad layout to zero width and height`() {
        val document = parse(
            """
            <layout xmlns:android="http://schemas.android.com/apk/res/android">
                <data />
                <LinearLayout android:id="@+id/mail_list_ad_view_container"
                    android:layout_width="match_parent" android:layout_height="72dp" />
            </layout>
            """.trimIndent(),
        )

        XmlTransforms.collapseTargets(document, "mail_list_ad")

        val view = document.getElementsByTagName("LinearLayout").item(0) as Element
        assertEquals("0dp", view.getAttribute("android:layout_width"))
        assertEquals("0dp", view.getAttribute("android:layout_height"))
        assertEquals("gone", view.getAttribute("android:visibility"))
    }

    @Test
    fun `collapses included drawer banner but preserves mail promotion controls`() {
        val document = parse(
            """
            <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android">
                <include android:id="@+id/banner" layout="@layout/drawer_banner_item" />
                <TextView android:id="@+id/menu_mail_promotion" />
            </LinearLayout>
            """.trimIndent(),
        )

        XmlTransforms.collapseTargets(document, "fragment_drawer")

        val include = document.getElementsByTagName("include").item(0) as Element
        val promotion = document.getElementsByTagName("TextView").item(0) as Element
        assertEquals("0dp", include.getAttribute("android:layout_width"))
        assertEquals("0dp", include.getAttribute("android:layout_height"))
        assertEquals("", promotion.getAttribute("android:visibility"))
    }

    @Test
    fun `collapses data bound drawer promotion ids without matching ordinary calendar banners`() {
        val document = parse(
            """
            <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android">
                <FrameLayout android:id="@+id/banner" />
                <FrameLayout android:id="@+id/incentive_cognition" />
                <FrameLayout android:id="@+id/guide_imap_login" />
                <FrameLayout android:id="@+id/guide_switch_gmail_account" />
                <FrameLayout android:id="@+id/target_text_position" />
                <FrameLayout android:id="@+id/calendar_banner_body" />
            </LinearLayout>
            """.trimIndent(),
        )

        XmlTransforms.collapseTargets(document, "fragment_drawer")

        val frames = document.getElementsByTagName("FrameLayout")
        assertEquals("gone", (frames.item(0) as Element).getAttribute("android:visibility"))
        assertEquals("gone", (frames.item(1) as Element).getAttribute("android:visibility"))
        assertEquals("gone", (frames.item(2) as Element).getAttribute("android:visibility"))
        assertEquals("gone", (frames.item(3) as Element).getAttribute("android:visibility"))
        assertEquals("gone", (frames.item(4) as Element).getAttribute("android:visibility"))
        assertEquals("", (frames.item(5) as Element).getAttribute("android:visibility"))
    }

    @Test
    fun `collapses data bound drawer promotion layouts at their view root`() {
        for (layoutName in listOf(
            "drawer_target_text_position_item",
            "drawer_incentive_layout",
            "guide_imap_login",
            "guide_switch_gmail_account",
            "side_bar_list_target_text_position_item",
        )) {
            val document = parse(
                """
                <layout xmlns:android="http://schemas.android.com/apk/res/android">
                    <data />
                    <FrameLayout android:id="@+id/container" />
                </layout>
                """.trimIndent(),
            )

            XmlTransforms.collapseTargets(document, layoutName)

            val root = document.getElementsByTagName("FrameLayout").item(0) as Element
            assertEquals("0dp", root.getAttribute("android:layout_width"))
            assertEquals("0dp", root.getAttribute("android:layout_height"))
            assertEquals("gone", root.getAttribute("android:visibility"))
        }
    }

    private fun parse(xml: String): Document = DocumentBuilderFactory.newInstance()
        .newDocumentBuilder()
        .parse(ByteArrayInputStream(xml.toByteArray()))
}
