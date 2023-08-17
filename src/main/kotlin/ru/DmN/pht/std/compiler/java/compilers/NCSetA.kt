package ru.DmN.pht.std.compiler.java.compilers

import org.objectweb.asm.Opcodes
import ru.DmN.pht.base.compiler.java.Compiler
import ru.DmN.pht.base.compiler.java.compilers.NodeCompiler
import ru.DmN.pht.base.compiler.java.ctx.CompilationContext
import ru.DmN.pht.base.parser.ast.NodeNodesList
import ru.DmN.pht.base.utils.Variable
import ru.DmN.pht.base.utils.VirtualType
import ru.DmN.pht.std.utils.load
import ru.DmN.pht.std.utils.loadCast
import ru.DmN.pht.std.utils.storeCast

object NCSetA : NodeCompiler<NodeNodesList>() {
    override fun calc(node: NodeNodesList, compiler: Compiler, ctx: CompilationContext): VirtualType? =
        if (ctx.type.method && ctx.type.body) {
            val name = compiler.computeStringConst(node, ctx)
            val variable = ctx.body!![name]
            if (variable == null) {
                if (ctx.type.clazz) {
                    ctx.clazz!!.clazz.fields.find { it.name == name }
                } else throw RuntimeException()
                null
            } else ctx.global.getType(compiler, variable.type ?: "java.lang.Object")
        } else null

    override fun compile(node: NodeNodesList, compiler: Compiler, ctx: CompilationContext, ret: Boolean): Variable? =
        if (ctx.type.method && ctx.type.body) {
            val mnode = ctx.method!!.node
            val name = compiler.computeStringConst(node.nodes.first(), ctx)
            val variable = ctx.body!![name]
            if (variable == null) {
                if (ctx.type.clazz) {
                    val clazz = ctx.clazz!!.clazz
                    val field = clazz.fields.find { it.name == name }!!
                    mnode.run {
                        if (field.static) {
                            compiler.compile(node.nodes.last(), ctx, true)!!.apply { loadCast(this, field.type, mnode) }
                            if (ret)
                                visitInsn(Opcodes.DUP)
                            visitFieldInsn(Opcodes.PUTSTATIC, clazz.className, name, field.type.desc)
                        } else {
                            visitVarInsn(Opcodes.ALOAD, 0) // "this" = 0
                            compiler.compile(node.nodes.last(), ctx, true)!!.apply { loadCast(this, field.type, mnode) }
                            if (ret)
                                visitInsn(Opcodes.DUP_X1)
                            visitFieldInsn(Opcodes.PUTFIELD, clazz.className, name, field.type.desc)
                        }
                    }
                    Variable("lul$${node.hashCode()}", field.type.name, -1, true)
                } else throw RuntimeException()
            } else {
                val type = compiler.compile(node.nodes.last(), ctx, true)!!.apply { load(this, mnode) }.type!!
                if (ret)
                    mnode.visitInsn(Opcodes.DUP)
                storeCast(variable, ctx.global.getType(compiler, type), mnode)
                Variable("lul$${node.hashCode()}", type, -1, true)
            }
        } else null
}