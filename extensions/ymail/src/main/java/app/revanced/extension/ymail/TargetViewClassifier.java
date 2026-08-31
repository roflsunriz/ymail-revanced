package app.revanced.extension.ymail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class TargetViewClassifier {
    private static final Set<String> DETACHED_PROMOTION_RESOURCE_NAMES = new HashSet<>(Arrays.asList(
            "banner",
            "drawer_banner",
            "footer_banner",
            "guide_imap_login",
            "guide_switch_gmail_account",
            "incentive_cognition",
            "side_bar_list_user_training_pr_container",
            "target_text_position"
    ));

    private static final Set<String> EXACT_RESOURCE_NAMES = new HashSet<>(Arrays.asList(
            "banner",
            "detail_ad_shadow",
            "detail_footer_ad",
            "divider_lyp_premium_setting_above",
            "drawer_banner",
            "footer_banner",
            "guide_imap_login",
            "guide_switch_gmail_account",
            "incentive_cognition",
            "layout_lyp_premium_aware_container",
            "lyp_premium_benefit_setting_container",
            "lyp_premium_icon",
            "lyp_premium_registration_setting_container",
            "mail_list_ad_view_container",
            "side_bar_list_user_training_pr_container",
            "target_text_position",
            "target_promotion_position"
    ));

    private static final String[] RESOURCE_PREFIXES = {
            "drawer_banner_", "list_ad_", "lyp_premium_", "mail_ad_", "mail_list_ad_",
            "message_list_ad_", "offline_ads_", "responsive_ad_", "sidebar_banner_",
            "sidebar_inducement_banner_", "target_promotion_position_", "ymail_promotion_image_",
            "ymail_promotion_web_", "ymail_sidebar_banner_", "ymail_sidebar_inducement_banner_",
            "yjadsdk_"
    };

    private static final String[] BLOCKED_CLASS_FRAGMENTS = {
            ".google.android.gms.ads.",
            ".google.android.gms.internal.ads.",
            ".yahoo.android.ads.",
            ".ymail.adsdk.",
            ".ymail.googlead.",
            ".ymail.presentation.maillist.ad."
    };

    private static final String[] BLOCKED_ACTIVITY_FRAGMENTS = {
            "LypPremiumBenefit",
            "LyLinkagePromotionModal",
            "UserTrainingPromotionWebView",
            "YJFeedbackPopupActivity",
            "YMailPromotionWebView"
    };

    private TargetViewClassifier() {
    }

    public static boolean isBlockedResourceName(String name) {
        if (name == null) return false;
        String normalized = name.toLowerCase(Locale.ROOT);
        if (EXACT_RESOURCE_NAMES.contains(normalized)) return true;
        for (String prefix : RESOURCE_PREFIXES) {
            if (normalized.startsWith(prefix)) return true;
        }
        return false;
    }

    public static boolean isBlockedViewClass(String className) {
        if (className == null) return false;
        String normalized = "." + className.toLowerCase(Locale.ROOT).replaceFirst("^\\.+", "");
        for (String fragment : BLOCKED_CLASS_FRAGMENTS) {
            if (normalized.contains(fragment)) return true;
        }
        return false;
    }

    public static boolean isBlockedActivityClass(String className) {
        if (className == null) return false;
        for (String fragment : BLOCKED_ACTIVITY_FRAGMENTS) {
            if (className.contains(fragment)) return true;
        }
        return false;
    }

    public static boolean shouldDetachPromotion(String resourceName) {
        return resourceName != null && DETACHED_PROMOTION_RESOURCE_NAMES.contains(
                resourceName.toLowerCase(Locale.ROOT));
    }

    public static boolean isBlockedHierarchy(
            String resourceName,
            Set<String> ancestorResourceNames,
            Set<String> directChildResourceNames) {
        if (directChildResourceNames.contains("banner_image")
                && directChildResourceNames.contains("banner_close_button")) {
            return true;
        }
        if (directChildResourceNames.contains("sidebar_icon")
                && directChildResourceNames.contains("body_text")
                && directChildResourceNames.contains("close_button")) {
            return true;
        }
        return "guide_container".equals(resourceName)
                && ancestorResourceNames.contains("drawer_items");
    }
}
