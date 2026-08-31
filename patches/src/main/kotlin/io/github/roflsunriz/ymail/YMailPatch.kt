package io.github.roflsunriz.ymail

import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val yMailPatch = bytecodePatch(
    name = "Yahoo!メール 広告除去",
    description = "広告SDK通信、広告枠、販促画面、販促通知を除去します。",
) {
    compatibleWith("jp.co.yahoo.android.ymail")
    dependsOn(yMailResourcePatch)
    extendWith("extensions/ymail.rve")

    apply {
        patchNetworkBoundaries()
        patchPromotionNotifications()
    }
}
