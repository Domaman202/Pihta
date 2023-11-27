package ru.DmN.pht.std.compiler.java.compilers

import ru.DmN.siberia.ast.Node
import ru.DmN.siberia.Compiler
import ru.DmN.siberia.compilers.INodeCompiler
import ru.DmN.siberia.compiler.ctx.CompilationContext

object NCSkip : INodeCompiler<Node> {
    override fun compile(node: Node, compiler: Compiler, ctx: CompilationContext) =
        Unit
}