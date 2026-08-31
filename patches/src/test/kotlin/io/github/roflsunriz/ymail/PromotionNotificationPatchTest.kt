package io.github.roflsunriz.ymail

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PromotionNotificationPatchTest {
    @Test
    fun `suppresses promotion only void methods`() {
        assertTrue(isExclusivePromotionMethod(setOf("promotion_notification", "survey_log"), "V"))
        assertTrue(isExclusivePromotionMethod(
            setOf("jp.co.yahoo.android.ymail.action.ACTION_PROMOTION_NOTIFICATION"),
            "V",
        ))
    }

    @Test
    fun `preserves dispatchers that also handle mail notifications`() {
        assertFalse(isExclusivePromotionMethod(setOf("promotion_notification", "new_mail"), "V"))
        assertFalse(isExclusivePromotionMethod(setOf("promotion_notification"), "Ljava/lang/Object;"))
    }
}
