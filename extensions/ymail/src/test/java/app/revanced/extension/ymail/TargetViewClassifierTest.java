package app.revanced.extension.ymail;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TargetViewClassifierTest {
    @Test
    void blocksAdvertisingAndCommercialPromotionViews() {
        assertTrue(TargetViewClassifier.isBlockedResourceName("mail_list_ad_view_container"));
        assertTrue(TargetViewClassifier.isBlockedResourceName("banner"));
        assertTrue(TargetViewClassifier.isBlockedResourceName("incentive_cognition"));
        assertTrue(TargetViewClassifier.isBlockedResourceName("guide_imap_login"));
        assertTrue(TargetViewClassifier.isBlockedResourceName("guide_switch_gmail_account"));
        assertTrue(TargetViewClassifier.isBlockedResourceName("side_bar_list_user_training_pr_container"));
        assertTrue(TargetViewClassifier.isBlockedResourceName("target_text_position"));
        assertTrue(TargetViewClassifier.isBlockedResourceName("lyp_premium_registration_setting_container"));
        assertTrue(TargetViewClassifier.isBlockedViewClass("jp.co.yahoo.android.ymail.adsdk.AdView"));
        assertTrue(TargetViewClassifier.isBlockedActivityClass(
                "jp.co.yahoo.android.ymail.nativeapp.activity.YMailPromotionWebViewActivity"));
        assertTrue(TargetViewClassifier.shouldDetachPromotion("banner"));
        assertTrue(TargetViewClassifier.shouldDetachPromotion("incentive_cognition"));
        assertTrue(TargetViewClassifier.shouldDetachPromotion("guide_imap_login"));
        assertTrue(TargetViewClassifier.shouldDetachPromotion("guide_switch_gmail_account"));
    }

    @Test
    void preservesMailPromotionCategoryAndNormalViews() {
        assertFalse(TargetViewClassifier.isBlockedResourceName("menu_mail_promotion"));
        assertFalse(TargetViewClassifier.isBlockedResourceName("calendar_banner_body"));
        assertFalse(TargetViewClassifier.isBlockedResourceName("inbox_or_promotion_bottom_divider"));
        assertFalse(TargetViewClassifier.isBlockedViewClass("androidx.recyclerview.widget.RecyclerView"));
        assertFalse(TargetViewClassifier.shouldDetachPromotion("mail_list_ad_view_container"));
    }

    @Test
    void blocksOnlyDrawerBannerAndDrawerGuideHierarchySignatures() {
        assertTrue(TargetViewClassifier.isBlockedHierarchy(
                "container", Set.of("drawer_items"), Set.of("banner_image", "banner_close_button")));
        assertTrue(TargetViewClassifier.isBlockedHierarchy(
                "guide_container", Set.of("drawer_items"), Set.of("guide_image", "guide_close_button")));
        assertTrue(TargetViewClassifier.isBlockedHierarchy(
                "container", Set.of("drawer_items"), Set.of("sidebar_icon", "title_text", "body_text", "close_button")));
        assertFalse(TargetViewClassifier.isBlockedHierarchy(
                "container", Set.of("calendar_root"), Set.of("calendar_banner_body")));
        assertFalse(TargetViewClassifier.isBlockedHierarchy(
                "container", Set.of("drawer_items"), Set.of("sidebar_icon", "title_text", "body_text")));
        assertFalse(TargetViewClassifier.isBlockedHierarchy(
                "guide_container", Set.of("mail_list"), Set.of("guide_image", "guide_close_button")));
    }
}
