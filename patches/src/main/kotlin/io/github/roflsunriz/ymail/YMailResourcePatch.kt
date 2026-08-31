package io.github.roflsunriz.ymail

import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatchContext
import app.revanced.patcher.patch.resourcePatch
import org.w3c.dom.Document
import org.w3c.dom.Element

private const val BOOTSTRAP_PROVIDER = "app.revanced.extension.ymail.BootstrapProvider"
private const val BOOTSTRAP_AUTHORITY = "jp.co.yahoo.android.ymail.revanced.bootstrap"

internal val yMailResourcePatch = resourcePatch {
    compatibleWith("jp.co.yahoo.android.ymail")

    apply {
        document("AndroidManifest.xml").use(Document::removeAdvertisingSurfaces)
        collapseAdvertisingLayouts()
    }
}

private fun ResourcePatchContext.collapseAdvertisingLayouts() {
    val resourceRoot = get("res")
    val layoutFiles = resourceRoot.listFiles()
        .orEmpty()
        .filter { it.isDirectory && (it.name == "layout" || it.name.startsWith("layout-")) }
        .flatMap { directory -> directory.listFiles().orEmpty().filter { it.isFile && it.extension == "xml" } }

    if (layoutFiles.isEmpty()) throw PatchException("Yahoo!メールのレイアウトリソースが見つかりません")

    layoutFiles.forEach { file ->
        val path = "res/${file.parentFile.name}/${file.name}"
        document(path).use { document ->
            XmlTransforms.collapseTargets(document, file.nameWithoutExtension)
        }
    }
}

internal fun Document.removeAdvertisingSurfaces() {
    val root = documentElement ?: throw PatchException("AndroidManifest.xmlにルート要素がありません")
    removeMatchingElements("uses-permission") { element ->
        TargetClassifier.isBlockedPermission(element.getAttribute("android:name"))
    }

    val application = getElementsByTagName("application").item(0) as? Element
        ?: throw PatchException("AndroidManifest.xmlにapplication要素がありません")

    listOf("activity", "activity-alias", "provider", "receiver", "service").forEach { tagName ->
        val components = application.getElementsByTagName(tagName)
        for (index in 0 until components.length) {
            val component = components.item(index) as? Element ?: continue
            if (TargetClassifier.isBlockedComponent(component.getAttribute("android:name"))) {
                component.setAttribute("android:enabled", "false")
                component.setAttribute("android:exported", "false")
            }
        }
    }

    removeMatchingElements("meta-data") { element ->
        val name = element.getAttribute("android:name")
        name == "com.google.android.gms.ads.APPLICATION_ID" ||
            name.startsWith("com.google.android.gms.ads.flag.") ||
            name == "com.google.android.play.billingclient.version" ||
            TargetClassifier.isBlockedFirebaseRegistrar(name)
    }

    mapOf(
        "firebase_analytics_collection_deactivated" to "true",
        "firebase_analytics_collection_enabled" to "false",
        "google_analytics_adid_collection_enabled" to "false",
        "google_analytics_automatic_screen_reporting_enabled" to "false",
        "firebase_crashlytics_collection_enabled" to "false",
        "firebase_performance_collection_enabled" to "false",
        "firebase_sessions_enabled" to "false",
    ).forEach { (name, value) -> application.upsertMetadata(this, name, value) }

    if (!application.hasComponent("provider", BOOTSTRAP_PROVIDER)) {
        application.appendChild(createElement("provider").apply {
            setAttribute("android:name", BOOTSTRAP_PROVIDER)
            setAttribute("android:authorities", BOOTSTRAP_AUTHORITY)
            setAttribute("android:enabled", "true")
            setAttribute("android:exported", "false")
            setAttribute("android:initOrder", "999999")
        })
    }

    check(root === documentElement)
}

private fun Document.removeMatchingElements(tagName: String, predicate: (Element) -> Boolean) {
    val elements = getElementsByTagName(tagName)
    for (index in elements.length - 1 downTo 0) {
        val element = elements.item(index) as? Element ?: continue
        if (predicate(element)) element.parentNode?.removeChild(element)
    }
}

private fun Element.upsertMetadata(document: Document, name: String, value: String) {
    val existing = (0 until childNodes.length)
        .mapNotNull { childNodes.item(it) as? Element }
        .firstOrNull { it.tagName == "meta-data" && it.getAttribute("android:name") == name }
    val metadata = existing ?: document.createElement("meta-data").also(::appendChild)
    metadata.setAttribute("android:name", name)
    metadata.setAttribute("android:value", value)
}

private fun Element.hasComponent(tagName: String, className: String): Boolean {
    val components = getElementsByTagName(tagName)
    return (0 until components.length).any { index ->
        (components.item(index) as? Element)?.getAttribute("android:name") == className
    }
}
