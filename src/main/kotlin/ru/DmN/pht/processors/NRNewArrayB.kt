package ru.DmN.pht.processors

import ru.DmN.pht.ast.NodeNewArray
import ru.DmN.siberia.Processor
import ru.DmN.siberia.processor.ctx.ProcessingContext
import ru.DmN.siberia.processors.INodeProcessor
import ru.DmN.siberia.utils.vtype.VirtualType

object NRNewArrayB : INodeProcessor<NodeNewArray> {
    override fun calc(node: NodeNewArray, processor: Processor, ctx: ProcessingContext): VirtualType =
        node.type.arrayType
}