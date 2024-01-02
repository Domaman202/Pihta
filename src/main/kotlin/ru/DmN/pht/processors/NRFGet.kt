package ru.DmN.pht.std.processors

import ru.DmN.pht.std.ast.NodeFGet
import ru.DmN.pht.std.ast.NodeFGet.Type.*
import ru.DmN.pht.std.utils.computeType
import ru.DmN.siberia.Processor
import ru.DmN.siberia.processor.ctx.ProcessingContext
import ru.DmN.siberia.processors.INodeProcessor
import ru.DmN.siberia.utils.VirtualField
import ru.DmN.siberia.utils.VirtualType

object NRFGet : INodeProcessor<NodeFGet> {
    override fun calc(node: NodeFGet, processor: Processor, ctx: ProcessingContext): VirtualType {
        val filter = when (node.type) {
            UNKNOWN  -> { _: VirtualField  -> true }
            STATIC   -> { it: VirtualField -> it.isStatic }
            INSTANCE -> { it: VirtualField -> !it.isStatic }
        }
        return (if (node.type == STATIC) processor.computeType(node.nodes[0], ctx) else processor.calc(node.nodes[0], ctx)!!)
            .fields
            .asSequence()
            .filter { it.name == node.name }
            .filter(filter)
            .first()
            .type
    }
}