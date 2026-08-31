package io.github.roflsunriz.ymail

import org.w3c.dom.Document
import org.w3c.dom.Element

internal object XmlTransforms {
    private val marginAttributes = listOf(
        "android:layout_margin",
        "android:layout_marginBottom",
        "android:layout_marginEnd",
        "android:layout_marginHorizontal",
        "android:layout_marginLeft",
        "android:layout_marginRight",
        "android:layout_marginStart",
        "android:layout_marginTop",
        "android:layout_marginVertical",
    )

    fun collapseTargets(document: Document, layoutName: String) {
        val root = document.documentElement ?: return
        if (TargetClassifier.isCollapsedLayout(layoutName)) {
            collapse(root.viewRoot())
        }

        val elements = document.getElementsByTagName("*")
        for (index in 0 until elements.length) {
            val element = elements.item(index) as? Element ?: continue
            if (isTarget(element)) collapse(element)
        }
    }

    private fun isTarget(element: Element): Boolean {
        val id = element.getAttribute("android:id").substringAfterLast('/')
        val layout = element.getAttribute("layout").substringAfterLast('/')
        val androidLayout = element.getAttribute("android:layout").substringAfterLast('/')
        val className = element.getAttribute("class").ifEmpty { element.tagName }
        return TargetClassifier.isBlockedResourceName(id) ||
            TargetClassifier.isCollapsedLayout(layout) ||
            TargetClassifier.isCollapsedLayout(androidLayout) ||
            TargetClassifier.isBlockedViewClass(className)
    }

    private fun Element.viewRoot(): Element {
        if (tagName != "layout") return this
        for (index in 0 until childNodes.length) {
            val child = childNodes.item(index) as? Element ?: continue
            if (child.tagName != "data") return child
        }
        return this
    }

    private fun collapse(element: Element) {
        element.setAttribute("android:layout_width", "0dp")
        element.setAttribute("android:layout_height", "0dp")
        element.setAttribute("android:minWidth", "0dp")
        element.setAttribute("android:minHeight", "0dp")
        element.setAttribute("android:visibility", "gone")
        marginAttributes.forEach { element.setAttribute(it, "0dp") }
    }
}
