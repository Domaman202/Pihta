package ru.DmN.pht.std.fp.compiler.java.compilers

import org.objectweb.asm.Opcodes
import ru.DmN.pht.base.compiler.java.Compiler
import ru.DmN.pht.base.compiler.java.compilers.INodeCompiler
import ru.DmN.pht.base.compiler.java.utils.CompilationContext
import ru.DmN.pht.base.utils.Variable
import ru.DmN.pht.std.base.compiler.java.utils.body
import ru.DmN.pht.std.base.compiler.java.utils.clazz
import ru.DmN.pht.std.base.compiler.java.utils.method
import ru.DmN.pht.std.value.ast.NodeGetOrName

object NCGetB : INodeCompiler<NodeGetOrName> {
    override fun compileVal(node: NodeGetOrName, compiler: Compiler, ctx: CompilationContext): Variable =
        ctx.body[node.name] ?: ctx.method.node.run {
            var clazz = ctx.clazz.clazz
            var field = clazz.fields.find { it.name == node.name }
            if (field == null) {
                clazz = compiler.tp.typeOf(node.name)
                field = clazz.fields.find { it.name == "INSTANCE" }!!
            }
            if (field.static)
                visitFieldInsn(Opcodes.GETSTATIC, clazz.className, field.name, field.desc)
            else {
                visitVarInsn(Opcodes.ALOAD, ctx.body["this"]!!.id)
                visitFieldInsn(Opcodes.GETFIELD, clazz.className, field.name, field.desc)
            }
            Variable.tmp(node, field.type)
        }
}