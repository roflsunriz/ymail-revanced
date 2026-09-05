package io.github.roflsunriz.ymail

import app.revanced.com.android.tools.smali.dexlib2.mutable.MutableClassDef
import app.revanced.com.android.tools.smali.dexlib2.mutable.MutableMethod
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter
import com.android.tools.smali.dexlib2.immutable.instruction.ImmutableInstruction10x
import com.android.tools.smali.dexlib2.immutable.instruction.ImmutableInstruction11n
import com.android.tools.smali.dexlib2.immutable.instruction.ImmutableInstruction35c
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference

internal const val MAIL_LIST_AD_CONTAINER =
    "Ljp/co/yahoo/android/ymail/presentation/maillist/ad/MailListAdViewContainer;"

internal fun BytecodePatchContext.patchAdViewLayouts() {
    // Older layouts without this custom container remain covered by the resource patch.
    val container = classDefs[MAIL_LIST_AD_CONTAINER] ?: return
    collapseAdViewMeasurement(classDefs.getOrReplaceMutable(container))
}

internal fun collapseAdViewMeasurement(container: MutableClassDef) {
    require(container.type == MAIL_LIST_AD_CONTAINER)
    val existing = container.methods.singleOrNull {
        it.name == "onMeasure" && it.parameterTypes == listOf("I", "I") && it.returnType == "V"
    }
    require(existing == null ||
        (!AccessFlags.STATIC.isSet(existing.accessFlags) && !AccessFlags.PRIVATE.isSet(existing.accessFlags))) {
        "Advertising container onMeasure must be a virtual instance method"
    }

    // Selection rebinds restore the row's LayoutParams.height before the global-layout
    // listener runs. Keep the measured size zero at that earlier layout boundary.
    // v0 = 0, p0 = v1, p1 = v2, p2 = v3; never overwrite the receiver or use invoke-super.
    val implementation = ImmutableMethodImplementation(
        4,
        listOf(
            ImmutableInstruction11n(Opcode.CONST_4, 0, 0),
            ImmutableInstruction35c(
                Opcode.INVOKE_VIRTUAL, 3, 1, 0, 0, 0, 0,
                ImmutableMethodReference("Landroid/view/View;", "setMeasuredDimension", listOf("I", "I"), "V"),
            ),
            ImmutableInstruction10x(Opcode.RETURN_VOID),
        ),
        emptyList(),
        emptyList(),
    )
    if (existing != null) {
        existing.accessFlags = existing.accessFlags and
            (AccessFlags.ABSTRACT.value or AccessFlags.NATIVE.value).inv()
        existing.implementation = MutableMethodImplementation(implementation)
    } else {
        container.methods.add(MutableMethod(ImmutableMethod(
            container.type,
            "onMeasure",
            List(2) { ImmutableMethodParameter("I", emptySet(), null) },
            "V",
            AccessFlags.PROTECTED.value,
            emptySet(),
            emptySet(),
            implementation,
        )))
    }
}
