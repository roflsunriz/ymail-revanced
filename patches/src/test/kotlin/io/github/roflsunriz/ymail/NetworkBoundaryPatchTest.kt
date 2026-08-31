package io.github.roflsunriz.ymail

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NetworkBoundaryPatchTest {
    @Test
    fun `void advertising SDK calls are disabled`() {
        assertTrue(shouldNoOpSdkCall("Lcom/google/android/gms/ads/AdView;", "loadAd", "V"))
        assertTrue(shouldNoOpSdkCall("Ljp/co/yahoo/android/ads/YJIIconInlineView;", "show", "V"))
        assertTrue(shouldNoOpSdkCall("Lcom/adjust/sdk/Adjust;", "trackEvent", "V"))
    }

    @Test
    fun `constructors return values and mail APIs are preserved`() {
        assertFalse(shouldNoOpSdkCall("Lcom/google/android/gms/ads/AdView;", "<init>", "V"))
        assertFalse(shouldNoOpSdkCall("Lcom/google/android/gms/ads/AdLoader;", "builder", "Ljava/lang/Object;"))
        assertFalse(shouldNoOpSdkCall("Lcom/google/firebase/sessions/FirebaseSessionsRegistrar;", "configure", "V"))
        assertFalse(shouldNoOpSdkCall("Lcom/google/firebase/crashlytics/FirebaseCrashlytics;", "setCollectionEnabled", "V"))
        assertFalse(shouldNoOpSdkCall("Ljp/co/yahoo/android/ymail/MailApi;", "sync", "V"))
    }
}
