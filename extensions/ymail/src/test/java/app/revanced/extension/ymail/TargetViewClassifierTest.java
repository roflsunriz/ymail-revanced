package app.revanced.extension.ymail;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TargetViewClassifierTest {
    @Test
    void blocksAdvertisingAndCommercialPromotionViews() {
        assertTrue(TargetViewClassifier.isBlockedResourceName("mail_list_ad_view_container"));
        assertTrue(TargetViewClassifier.isBlockedResourceName("lyp_premium_registration_setting_container"));
        assertTrue(TargetViewClassifier.isBlockedViewClass("jp.co.yahoo.android.ymail.adsdk.AdView"));
        assertTrue(TargetViewClassifier.isBlockedActivityClass(
                "jp.co.yahoo.android.ymail.nativeapp.activity.YMailPromotionWebViewActivity"));
    }

    @Test
    void preservesMailPromotionCategoryAndNormalViews() {
        assertFalse(TargetViewClassifier.isBlockedResourceName("menu_mail_promotion"));
        assertFalse(TargetViewClassifier.isBlockedResourceName("inbox_or_promotion_bottom_divider"));
        assertFalse(TargetViewClassifier.isBlockedViewClass("androidx.recyclerview.widget.RecyclerView"));
    }
}
