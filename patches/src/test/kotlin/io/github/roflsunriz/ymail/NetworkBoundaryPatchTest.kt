package io.github.roflsunriz.ymail

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.immutable.instruction.ImmutableInstruction35c
import com.android.tools.smali.dexlib2.immutable.instruction.ImmutableInstruction3rc
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference

class NetworkBoundaryPatchTest {
    @Test
    fun `WebView super calls preserve dispatch and sanitize only the URL register`() {
        for (headers in listOf(false, true)) {
            val reference = ImmutableMethodReference(
                "Landroid/webkit/WebView;", "loadUrl",
                if (headers) listOf("Ljava/lang/String;", "Ljava/util/Map;") else listOf("Ljava/lang/String;"),
                "V",
            )
            for (range in listOf(false, true)) {
                val count = if (headers) 3 else 2
                val instruction = if (range) {
                    ImmutableInstruction3rc(Opcode.INVOKE_SUPER_RANGE, 20, count, reference)
                } else {
                    ImmutableInstruction35c(Opcode.INVOKE_SUPER, count, 2, 5, 8, 0, 0, reference)
                }
                for (blockAll in listOf(false, true)) {
                    val rewrite = assertInstanceOf(
                        NetworkRewrite.Insert::class.java,
                        networkRewrite(7, instruction, reference, blockAll),
                    )
                    val register = if (range) 21 else 5
                    val helper = if (blockAll) "blockNetworkUrl" else "sanitizeNetworkUrl"
                    assertEquals(7, rewrite.index)
                    assertEquals(
                        (if (range) "invoke-static/range { v21 .. v21 }" else "invoke-static { v5 }") +
                            ", Lapp/revanced/extension/ymail/AdBlocker;->$helper(Ljava/lang/String;)Ljava/lang/String;\n" +
                            "move-result-object v$register",
                        rewrite.smali,
                    )
                }
            }
        }
    }

    @Test
    fun `ordinary WebView calls still use the network wrapper`() {
        val reference = ImmutableMethodReference(
            "Landroid/webkit/WebView;", "loadUrl", listOf("Ljava/lang/String;"), "V",
        )
        val instruction = ImmutableInstruction35c(Opcode.INVOKE_VIRTUAL, 2, 0, 1, 0, 0, 0, reference)
        val rewrite = assertInstanceOf(NetworkRewrite.Replace::class.java,
            networkRewrite(0, instruction, reference, false))
        assertTrue(rewrite.smali.contains("->loadUrl(Landroid/webkit/WebView;Ljava/lang/String;)V"))
    }

    @Test
    fun `void advertising SDK calls are disabled`() {
        assertTrue(shouldNoOpSdkCall("Lcom/google/android/gms/ads/AdView;", "loadAd", "V"))
        assertTrue(shouldNoOpSdkCall("Lcom/adjust/sdk/Adjust;", "trackEvent", "V"))
    }

    @Test
    fun `constructors return values and mail APIs are preserved`() {
        assertFalse(shouldNoOpSdkCall("Lcom/google/android/gms/ads/AdView;", "<init>", "V"))
        assertFalse(shouldNoOpSdkCall("Lcom/google/android/gms/ads/AdLoader;", "builder", "Ljava/lang/Object;"))
        assertFalse(shouldNoOpSdkCall("Lcom/google/firebase/sessions/FirebaseSessionsRegistrar;", "configure", "V"))
        assertFalse(shouldNoOpSdkCall("Lcom/google/firebase/crashlytics/FirebaseCrashlytics;", "setCollectionEnabled", "V"))
        assertFalse(shouldNoOpSdkCall("Ljp/co/yahoo/android/ymail/adsdk/AdView;", "setAdTheme", "V"))
        assertFalse(shouldNoOpSdkCall("Ljp/co/yahoo/android/ads/YJIIconInlineView;", "show", "V"))
        assertFalse(shouldNoOpSdkCall("Ljp/co/yahoo/android/ymail/MailApi;", "sync", "V"))
    }
}
