package ru.DmN.pht.processors

import ru.DmN.pht.processor.utils.computeString
import ru.DmN.pht.processor.utils.computeType
import ru.DmN.pht.utils.node.*
import ru.DmN.pht.utils.vtype.arrayType
import ru.DmN.siberia.ast.Node
import ru.DmN.siberia.ast.NodeNodesList
import ru.DmN.siberia.processor.Processor
import ru.DmN.siberia.processor.ctx.ProcessingContext
import ru.DmN.siberia.processors.INodeProcessor
import ru.DmN.siberia.utils.Variable
import ru.DmN.siberia.utils.vtype.VirtualType

object NRArrayOfType : INodeProcessor<NodeNodesList> {
    override fun calc(node: NodeNodesList, processor: Processor, ctx: ProcessingContext): VirtualType? =
        processor.computeType(node.nodes[0], ctx).arrayType

    override fun process(node: NodeNodesList, processor: Processor, ctx: ProcessingContext, valMode: Boolean): Node? =
        if (valMode) {
            val info = node.info
            val tmp = Variable.tmp(node)
            val type = processor.computeString(node.nodes[0], ctx)
            processor.process(
                nodeBody(info, ArrayList<Node>().apply {
                    this.add(nodeDef(info, tmp, nodeNewArray(info, type, node.nodes.size - 1)))
                    this.addAll(
                        node.nodes.asSequence().drop(1)
                            .mapIndexed { i, it -> nodeASet(info, tmp, i, nodeAs(info, it, type)) })
                    this.add(nodeGetOrName(info, tmp))
                }),
                ctx,
                true
            )
        } else null
}