package ru.DmN.pht.std.ups

import ru.DmN.siberia.Processor
import ru.DmN.siberia.Unparser
import ru.DmN.siberia.processor.ctx.ProcessingContext
import ru.DmN.siberia.unparser.UnparsingContext
import ru.DmN.siberia.unparsers.NUDefault
import ru.DmN.siberia.utils.VirtualType
import ru.DmN.pht.std.ast.NodeNew
import ru.DmN.siberia.utils.INUP
import ru.DmN.pht.std.unparsers.NUDefaultX
import ru.DmN.pht.std.utils.IStdNUP

object NUPNew : IStdNUP<NodeNew, NodeNew> {
    override fun unparse(node: NodeNew, unparser: Unparser, ctx: UnparsingContext, indent: Int) {
        unparser.out.apply {
            append('(').append(NUDefaultX.text(node.token)).append(' ').append(NUPValueA.unparseType(node.ctor.declaringClass!!.name))
            NUDefault.unparseNodes(node, unparser, ctx, indent)
            append(')')
        }
    }

    override fun calc(node: NodeNew, processor: Processor, ctx: ProcessingContext): VirtualType? =
        node.ctor.declaringClass

    override fun computeGenerics(node: NodeNew, processor: Processor, ctx: ProcessingContext): List<VirtualType> =
        node.generics
}