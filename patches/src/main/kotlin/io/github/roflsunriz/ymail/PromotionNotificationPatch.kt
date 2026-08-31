package io.github.roflsunriz.ymail

import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.instructionsOrNull
import app.revanced.patcher.firstMethod
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference

private val promotionMarkers = setOf(
    "jp.co.yahoo.android.ymail.action.ACTION_PROMOTION_NOTIFICATION",
    "jp.co.yahoo.android.ymail.action.DELETE_PROMOTION_NOTIFICATION",
    "load_promotion_notification",
    "promotion_notification",
)

private val nonPromotionNotificationMarkers = setOf(
    "highlight_notification",
    "new_feature_notification",
    "new_mail",
    "remote_notification",
)

internal fun BytecodePatchContext.patchPromotionNotifications() {
    classDefs.flatMap { it.methods }.filter(Method::isExclusivePromotionMethod).forEach { method ->
        firstMethod(method).addInstruction(0, "return-void")
    }
}

internal fun isExclusivePromotionMethod(stringLiterals: Set<String>, returnType: String): Boolean =
    returnType == "V" &&
        stringLiterals.any { literal -> promotionMarkers.any(literal::contains) } &&
        stringLiterals.none { literal -> nonPromotionNotificationMarkers.any(literal::contains) }

private fun Method.isExclusivePromotionMethod(): Boolean =
    isExclusivePromotionMethod(stringLiterals(), returnType)

private fun Method.stringLiterals(): Set<String> {
    val instructions = instructionsOrNull ?: return emptySet()
    return instructions.mapNotNull { instruction ->
        ((instruction as? ReferenceInstruction)?.reference as? StringReference)?.string
    }.toSet()
}
