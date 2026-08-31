package io.github.roflsunriz.ymail

internal object TargetClassifier {
    private val collapsedLayouts = setOf(
        "drawer_banner_item",
        "lyp_premium_aware_dialog",
        "mail_list_ad",
        "mail_list_ad_empty",
        "mail_list_ad_loading",
        "mail_list_ad_muted",
        "message_list_ad",
        "message_list_ad_empty",
        "message_list_ad_item",
        "message_list_ad_item_dynamic",
        "message_list_ad_item_dynamic_unread_style",
        "message_list_ad_item_responsive",
        "message_list_ad_item_responsive_unread_style",
        "message_list_ad_loading",
        "message_list_ad_muted",
        "offline_ads_dialog",
        "renewal_lyp_premium_aware_dialog",
        "target_promotion_position",
        "ymail_promotion_image_dialog",
        "ymail_promotion_web_view_dialog",
        "ymail_sidebar_banner",
        "ymail_sidebar_inducement_banner",
    )

    private val exactResourceNames = setOf(
        "detail_ad_shadow",
        "detail_footer_ad",
        "divider_lyp_premium_setting_above",
        "drawer_banner",
        "footer_banner",
        "layout_lyp_premium_aware_container",
        "lyp_premium_benefit_setting_container",
        "lyp_premium_icon",
        "lyp_premium_registration_setting_container",
        "mail_list_ad_view_container",
        "target_promotion_position",
    )

    private val resourcePrefixes = listOf(
        "drawer_banner_",
        "list_ad_",
        "lyp_premium_",
        "mail_ad_",
        "mail_list_ad_",
        "message_list_ad_",
        "offline_ads_",
        "responsive_ad_",
        "sidebar_banner_",
        "sidebar_inducement_banner_",
        "target_promotion_position_",
        "ymail_promotion_image_",
        "ymail_promotion_web_",
        "ymail_sidebar_banner_",
        "ymail_sidebar_inducement_banner_",
        "yjadsdk_",
    )

    private val blockedClassFragments = listOf(
        ".google.android.gms.ads.",
        ".google.android.gms.internal.ads.",
        ".yahoo.android.ads.",
        ".ymail.adsdk.",
        ".ymail.googlead.",
        ".ymail.presentation.maillist.ad.",
    )

    private val blockedComponentFragments = listOf(
        "com.android.billingclient.",
        "com.adjust.sdk.",
        "com.google.android.gms.ads.",
        "jp.co.yahoo.android.ads.",
        "LypPremiumBenefit",
        "LyLinkagePromotionModal",
        "UserTrainingPromotionWebView",
        "YMailPromotionWebView",
    )

    private val blockedSdkDescriptors = listOf(
        "Lcom/adjust/sdk/",
        "Lcom/google/ads/",
        "Lcom/google/android/gms/ads/",
        "Lcom/google/android/gms/internal/ads/",
        "Lcom/google/firebase/analytics/",
        "Lcom/google/firebase/crashlytics/",
        "Lcom/google/firebase/sessions/",
        "Ljp/co/yahoo/android/ads/",
        "Ljp/co/yahoo/android/ymail/adsdk/",
        "Ljp/co/yahoo/android/ymail/googlead/",
    )

    private val blockedPermissions = setOf(
        "android.permission.ACCESS_ADSERVICES_AD_ID",
        "android.permission.ACCESS_ADSERVICES_ATTRIBUTION",
        "android.permission.ACCESS_ADSERVICES_TOPICS",
        "com.android.vending.BILLING",
        "com.google.android.gms.permission.AD_ID",
        "com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE",
    )

    fun isCollapsedLayout(name: String): Boolean = name in collapsedLayouts

    fun isBlockedResourceName(name: String): Boolean {
        val normalized = name.lowercase()
        if (normalized in exactResourceNames || normalized in collapsedLayouts) return true
        return resourcePrefixes.any(normalized::startsWith)
    }

    fun isBlockedViewClass(className: String): Boolean {
        val normalized = ".${className.lowercase().trimStart('.')}"
        return blockedClassFragments.any(normalized::contains)
    }

    fun isBlockedComponent(className: String): Boolean =
        blockedComponentFragments.any(className::contains)

    fun isBlockedSdkDescriptor(descriptor: String): Boolean =
        blockedSdkDescriptors.any(descriptor::startsWith)

    fun isBlockedPermission(permission: String): Boolean = permission in blockedPermissions

}
