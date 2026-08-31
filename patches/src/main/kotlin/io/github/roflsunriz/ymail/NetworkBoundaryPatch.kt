package io.github.roflsunriz.ymail

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.RegisterRangeInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val AD_BLOCKER = "Lapp/revanced/extension/ymail/AdBlocker;"
private const val EXTENSION_PREFIX = "Lapp/revanced/extension/ymail/"

private sealed class NetworkRewrite(open val index: Int) {
    data class Replace(override val index: Int, val smali: String) : NetworkRewrite(index)
    data class Insert(override val index: Int, val smali: String) : NetworkRewrite(index)
}

internal fun BytecodePatchContext.patchNetworkBoundaries() {
    transformInstructions(
        match = { classDef, _, instruction, index ->
            if (classDef.type.startsWith(EXTENSION_PREFIX)) return@transformInstructions null
            val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference
                ?: return@transformInstructions null
            networkRewrite(
                index,
                instruction,
                reference,
                TargetClassifier.isBlockedSdkDescriptor(classDef.type),
            )
        },
        transform = { method, rewrite ->
            when (rewrite) {
                is NetworkRewrite.Replace -> method.replaceInstruction(rewrite.index, rewrite.smali)
                is NetworkRewrite.Insert -> method.addInstructions(rewrite.index, rewrite.smali)
            }
        },
    )
}

private fun networkRewrite(
    index: Int,
    instruction: Instruction,
    reference: MethodReference,
    blockAll: Boolean,
): NetworkRewrite? {
    if (shouldNoOpSdkCall(reference.definingClass, reference.name, reference.returnType)) {
        return NetworkRewrite.Replace(index, "nop")
    }

    val registers = instruction.argumentRegisters() ?: return null
    val signature = reference.toString()

    return when (signature) {
        "Ljava/net/InetAddress;->getAllByName(Ljava/lang/String;)[Ljava/net/InetAddress;" ->
            NetworkRewrite.Replace(index, registers.staticInvoke(instruction, "$AD_BLOCKER->${if (blockAll) "blockGetAllByName" else "getAllByName"}(Ljava/lang/String;)[Ljava/net/InetAddress;"))

        "Ljava/net/InetAddress;->getByName(Ljava/lang/String;)Ljava/net/InetAddress;" ->
            NetworkRewrite.Replace(index, registers.staticInvoke(instruction, "$AD_BLOCKER->${if (blockAll) "blockGetByName" else "getByName"}(Ljava/lang/String;)Ljava/net/InetAddress;"))

        "Ljava/net/URL;->openConnection()Ljava/net/URLConnection;" ->
            NetworkRewrite.Replace(index, registers.staticInvoke(instruction, "$AD_BLOCKER->${if (blockAll) "blockOpenConnection" else "openConnection"}(Ljava/net/URL;)Ljava/net/URLConnection;"))

        "Ljava/net/URL;->openConnection(Ljava/net/Proxy;)Ljava/net/URLConnection;" ->
            NetworkRewrite.Replace(index, registers.staticInvoke(instruction, "$AD_BLOCKER->${if (blockAll) "blockOpenConnection" else "openConnection"}(Ljava/net/URL;Ljava/net/Proxy;)Ljava/net/URLConnection;"))

        "Ljava/net/URLConnection;->connect()V",
        "Ljava/net/HttpURLConnection;->connect()V",
        "Ljavax/net/ssl/HttpsURLConnection;->connect()V" -> if (blockAll) {
            NetworkRewrite.Replace(index, registers.staticInvoke(instruction, "$AD_BLOCKER->blockConnect(Ljava/net/URLConnection;)V"))
        } else {
            null
        }

        "Landroid/webkit/WebView;->loadUrl(Ljava/lang/String;)V" ->
            NetworkRewrite.Replace(index, registers.staticInvoke(instruction, "$AD_BLOCKER->${if (blockAll) "blockLoadUrl" else "loadUrl"}(Landroid/webkit/WebView;Ljava/lang/String;)V"))

        "Landroid/webkit/WebView;->loadUrl(Ljava/lang/String;Ljava/util/Map;)V" ->
            NetworkRewrite.Replace(index, registers.staticInvoke(instruction, "$AD_BLOCKER->${if (blockAll) "blockLoadUrl" else "loadUrl"}(Landroid/webkit/WebView;Ljava/lang/String;Ljava/util/Map;)V"))

        else -> stringUrlRewrite(index, instruction, reference, registers, blockAll)
    }
}

internal fun shouldNoOpSdkCall(definingClass: String, name: String, returnType: String): Boolean =
    returnType == "V" && when {
        definingClass.startsWith("Lcom/google/android/gms/ads/") ||
            definingClass.startsWith("Lcom/google/ads/") ->
            name in setOf("initialize", "loadAd", "loadAds", "preload")
        definingClass.startsWith("Lcom/adjust/sdk/") ->
            name in setOf(
                "trackEvent",
                "trackAdRevenue",
                "trackPlayStoreSubscription",
                "trackThirdPartySharing",
                "gdprForgetMe",
                "disableThirdPartySharing",
                "sendFirstPackages",
            )
        else -> false
    }

private fun stringUrlRewrite(
    index: Int,
    instruction: Instruction,
    reference: MethodReference,
    registers: List<Int>,
    blockAll: Boolean,
): NetworkRewrite? {
    val stringParameterIndex = when {
        reference.definingClass == "Ljava/net/URL;" && reference.name == "<init>" &&
            reference.parameterTypes.firstOrNull()?.toString() == "Ljava/lang/String;" -> 0
        reference.definingClass == "Ljava/net/URI;" && reference.name == "create" -> 0
        reference.definingClass.startsWith("Lokhttp3/HttpUrl") &&
            reference.parameterTypes.firstOrNull()?.toString() == "Ljava/lang/String;" -> 0
        reference.name in setOf("url", "newUrlRequestBuilder", "postUrl", "loadDataWithBaseURL") &&
            reference.parameterTypes.firstOrNull()?.toString() == "Ljava/lang/String;" -> 0
        else -> return null
    }

    val parameterOffset = if (instruction.isStaticInvoke()) 0 else 1
    val valueRegister = registers.getOrNull(parameterOffset + stringParameterIndex) ?: return null
    val invoke = if (valueRegister > 15 || instruction is RegisterRangeInstruction) {
        "invoke-static/range { v$valueRegister .. v$valueRegister }"
    } else {
        "invoke-static { v$valueRegister }"
    }
    return NetworkRewrite.Insert(
        index,
        """
            $invoke, $AD_BLOCKER->${if (blockAll) "blockNetworkUrl" else "sanitizeNetworkUrl"}(Ljava/lang/String;)Ljava/lang/String;
            move-result-object v$valueRegister
        """.trimIndent(),
    )
}

private fun Instruction.argumentRegisters(): List<Int>? = when (this) {
    is Instruction35c -> listOf(registerC, registerD, registerE, registerF, registerG).take(registerCount)
    is RegisterRangeInstruction -> (startRegister until startRegister + registerCount).toList()
    else -> null
}

private fun Instruction.isStaticInvoke(): Boolean =
    opcode == Opcode.INVOKE_STATIC || opcode == Opcode.INVOKE_STATIC_RANGE

private fun List<Int>.staticInvoke(instruction: Instruction, target: String): String {
    val mnemonic = if (instruction is RegisterRangeInstruction) "invoke-static/range" else "invoke-static"
    val arguments = if (instruction is RegisterRangeInstruction && isNotEmpty()) {
        "v${first()} .. v${last()}"
    } else {
        joinToString(", ") { "v$it" }
    }
    return "$mnemonic { $arguments }, $target"
}
