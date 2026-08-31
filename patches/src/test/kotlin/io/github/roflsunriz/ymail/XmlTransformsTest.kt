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

    private fun parse(xml: String): Document = DocumentBuilderFactory.newInstance()
        .newDocumentBuilder()
        .parse(ByteArrayInputStream(xml.toByteArray()))
}
