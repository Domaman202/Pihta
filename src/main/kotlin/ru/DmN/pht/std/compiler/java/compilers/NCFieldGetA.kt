package ru.DmN.pht.std.compiler.java.compilers

import org.objectweb.asm.Opcodes
import ru.DmN.pht.base.compiler.java.Compiler
import ru.DmN.pht.base.compiler.java.compilers.NodeCompiler
import ru.DmN.pht.base.compiler.java.ctx.CompilationContext
import ru.DmN.pht.base.parser.ast.Node
import ru.DmN.pht.base.parser.ast.NodeNodesList
import ru.DmN.pht.base.utils.Variable
import ru.DmN.pht.base.utils.VirtualType
import ru.DmN.pht.std.utils.load

object NCFieldGetA : NodeCompiler<NodeNodesList>() {
    override fun calc(node: NodeNodesList, compiler: Compiler, ctx: CompilationContext): VirtualType? =
        if (ctx.type.method) {
            val name = compiler.computeStringConst(node.nodes.last(), ctx)
            (when (val type = compiler.compute<Any?>(node.nodes.first(), ctx, true)) {
                is String -> ctx.global.getType(compiler, type)
                is Node -> compiler.calc(type, ctx)!!
                else -> throw RuntimeException()
            }).fields.find { it.name == name }!!.type
        } else null

    override fun compile(node: NodeNodesList, compiler: Compiler, ctx: CompilationContext, ret: Boolean): Variable? =
        if (ret && ctx.type.method) {
            val name = compiler.computeStringConst(node.nodes.last(), ctx)
            when (val type = compiler.compute<Any?>(node.nodes.first(), ctx, true)) {
                is String -> {
                    val vtype = ctx.global.getType(compiler, type)
                    val field = vtype.fields.find { it.name == name }!!
                    ctx.method!!.node.visitFieldInsn(Opcodes.GETSTATIC, vtype.className, name, field.desc)
                    Variable("lul$${node.hashCode()}", field.type.name, -1, true)
                }

                is Node -> {
                    ctx.method!!.node.run {
                        val instance = compiler.compile(type, ctx, true)!!
                        load(instance, this)
                        val instanceType = ctx.global.getType(compiler, instance.type!!)
                        val field = instanceType.fields.find { it.name == name }!!
                        visitFieldInsn(Opcodes.GETFIELD, instanceType.className, name, field.desc)
                        Variable("lul$${node.hashCode()}", field.type.name, -1, true)
                    }
                }

                else -> throw RuntimeException()
            }
        } else null
}