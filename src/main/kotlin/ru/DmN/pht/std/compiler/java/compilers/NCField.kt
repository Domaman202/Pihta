package ru.DmN.pht.std.compiler.java.compilers

import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.FieldNode
import ru.DmN.pht.base.compiler.java.Compiler
import ru.DmN.pht.base.compiler.java.compilers.NodeCompiler
import ru.DmN.pht.base.compiler.java.ctx.CompilationContext
import ru.DmN.pht.base.compiler.java.ctx.FieldContext
import ru.DmN.pht.base.parser.ast.Node
import ru.DmN.pht.base.parser.ast.NodeNodesList
import ru.DmN.pht.base.utils.Variable
import ru.DmN.pht.base.utils.VirtualField

object NCField : NodeCompiler<NodeNodesList>() {
    override fun compile(node: NodeNodesList, compiler: Compiler, ctx: CompilationContext, ret: Boolean): Variable? {
        val nodes = node.nodes.map { it -> compiler.compute<List<Node>>(it, ctx, true).map { compiler.computeStringConst(it, ctx) } }
        if (ctx.type.method && ctx.type.body) {
            val label = Label()
            ctx.method!!.node.visitLabel(label)
            nodes.forEach { ctx.method.createVariable(ctx.body!!, it.first(), it.last(), label) }
        } else if (ctx.type.clazz) {
            val cctx = ctx.clazz!!
            val static = node.attributes.getOrDefault("static", false) as Boolean
            nodes.forEach {
                var access = Opcodes.ACC_PUBLIC
                if (node.attributes.getOrDefault("final", false) as Boolean)
                    access += Opcodes.ACC_FINAL
                if (static)
                    access += Opcodes.ACC_STATIC
                val type = ctx.clazz.getType(compiler, ctx.global, it.last())
                val fnode = cctx.node.visitField(access, it.first(), type.desc, type.signature, null) as FieldNode
                val field = VirtualField(it.first(), type, static, false)
                cctx.clazz.fields += field
                cctx.fields += FieldContext(fnode, field)
            }
        }
        return null
    }

    override fun applyAnnotation(node: NodeNodesList, compiler: Compiler, ctx: CompilationContext, annotation: Node) {
        when (annotation.tkOperation.text) {
            "@final" -> node.attributes["final"] = true
            "@static" -> node.attributes["static"] = true
        }
    }
}