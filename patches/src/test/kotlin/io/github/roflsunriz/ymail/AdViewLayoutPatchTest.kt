package io.github.roflsunriz.ymail

import app.revanced.com.android.tools.smali.dexlib2.mutable.MutableClassDef
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.Opcodes
import com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.ImmutableClassDef
import com.android.tools.smali.dexlib2.immutable.ImmutableDexFile
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter
import com.android.tools.smali.dexlib2.immutable.instruction.ImmutableInstruction10x
import com.android.tools.smali.dexlib2.writer.io.MemoryDataStore
import com.android.tools.smali.dexlib2.writer.pool.DexPool
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AdViewLayoutPatchTest {
    @Test
    fun `new override measures zero despite restored exact row dimensions`() {
        val container = container()
        collapseAdViewMeasurement(container)
        val method = roundTrip(container).single { it.name == "onMeasure" }
        assertTrue(AccessFlags.PROTECTED.isSet(method.accessFlags))
        for ((width, height) in listOf(1080 to 144, 1440 to 216, 720 to 96, 0 to 0)) {
            assertEquals(0 to 0, measure(method, 0x40000000 or width, 0x40000000 or height))
        }
    }

    @Test
    fun `replaces existing override without duplicating it or changing unrelated methods`() {
        val unrelated = method("setVisibility", listOf("I"))
        val container = container(listOf(method("onMeasure", listOf("I", "I")), unrelated))
        collapseAdViewMeasurement(container)
        collapseAdViewMeasurement(container)
        val methods = roundTrip(container)
        assertEquals(2, methods.size)
        assertEquals(0 to 0, measure(methods.single { it.name == "onMeasure" }, 1080, 256))
        assertEquals(unrelated, methods.single { it.name == "setVisibility" })
        assertEquals(listOf(Opcode.RETURN_VOID),
            methods.single { it.name == "setVisibility" }.implementation!!.instructions.map { it.opcode })
    }

    @Test
    fun `rejects a non virtual matching signature instead of generating invalid receiver use`() {
        val invalid = method("onMeasure", listOf("I", "I"), AccessFlags.STATIC.value)
        assertThrows(IllegalArgumentException::class.java) {
            collapseAdViewMeasurement(container(listOf(invalid)))
        }
    }

    @Test
    fun `does not target ordinary mail containers`() {
        val normal = MutableClassDef(ImmutableClassDef(
            "Lapp/MailContainer;", AccessFlags.PUBLIC.value, "Landroid/widget/RelativeLayout;",
            emptyList(), null, emptySet(), emptyList(), emptyList(),
        ))
        assertThrows(IllegalArgumentException::class.java) { collapseAdViewMeasurement(normal) }
        assertTrue(normal.methods.isEmpty())
    }

    private fun container(methods: List<Method> = emptyList()) = MutableClassDef(ImmutableClassDef(
        MAIL_LIST_AD_CONTAINER, AccessFlags.PUBLIC.value, "Landroid/widget/RelativeLayout;",
        emptyList(), null, emptySet(), emptyList(), methods,
    ))

    private fun method(name: String, parameters: List<String>, flags: Int = AccessFlags.PUBLIC.value) =
        ImmutableMethod(
            MAIL_LIST_AD_CONTAINER, name,
            parameters.map { ImmutableMethodParameter(it, emptySet(), null) }, "V", flags,
            emptySet(), emptySet(), ImmutableMethodImplementation(
                parameters.size + 1, listOf(ImmutableInstruction10x(Opcode.RETURN_VOID)),
                emptyList(), emptyList(),
            ),
        )

    private fun roundTrip(container: MutableClassDef): List<Method> {
        val opcodes = Opcodes.getDefault()
        val store = MemoryDataStore()
        DexPool.writeTo(store, ImmutableDexFile(opcodes, listOf(container)))
        val emitted = DexBackedDexFile(opcodes, store.data).classes.single()
        assertFalse(emitted.directMethods.any { it.name == "onMeasure" })
        return emitted.virtualMethods.toList()
    }

    // Execute the emitted straight-line method against a measured-size receiver. This
    // catches wrong receiver/argument registers as well as accidentally using MeasureSpecs.
    private fun measure(method: Method, widthSpec: Int, heightSpec: Int): Pair<Int, Int> {
        val body = method.implementation!!
        val registers = arrayOfNulls<Any>(body.registerCount)
        val receiver = Any()
        val parameterStart = body.registerCount - 3
        registers[parameterStart] = receiver
        registers[parameterStart + 1] = widthSpec
        registers[parameterStart + 2] = heightSpec
        var measured: Pair<Int, Int>? = null
        for (instruction in body.instructions) {
            when (instruction.opcode) {
                Opcode.CONST_4 -> registers[(instruction as OneRegisterInstruction).registerA] =
                    (instruction as NarrowLiteralInstruction).narrowLiteral
                Opcode.INVOKE_VIRTUAL -> {
                    val reference = (instruction as ReferenceInstruction).reference as MethodReference
                    assertEquals("Landroid/view/View;", reference.definingClass)
                    assertEquals("setMeasuredDimension", reference.name)
                    assertEquals(listOf("I", "I"), reference.parameterTypes)
                    assertEquals("V", reference.returnType)
                    val args = instruction as FiveRegisterInstruction
                    assertEquals(3, args.registerCount)
                    assertSame(receiver, registers[args.registerC])
                    measured = (registers[args.registerD] as Int) to (registers[args.registerE] as Int)
                }
                Opcode.RETURN_VOID -> return requireNotNull(measured)
                else -> error("Unexpected measurement instruction: ${instruction.opcode}")
            }
        }
        error("Measurement did not return")
    }
}
