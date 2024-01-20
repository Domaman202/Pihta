package ru.DmN.pht.std.processors

import ru.DmN.pht.std.node.nodeArrayOfType
import ru.DmN.pht.std.node.nodeMCall
import ru.DmN.pht.std.processor.utils.global
import ru.DmN.siberia.Processor
import ru.DmN.siberia.ast.Node
import ru.DmN.siberia.ast.NodeNodesList
import ru.DmN.siberia.processor.ctx.ProcessingContext
import ru.DmN.siberia.processor.utils.Platforms.JAVA
import ru.DmN.siberia.processor.utils.ValType
import ru.DmN.siberia.processor.utils.ValType.VALUE
import ru.DmN.siberia.processor.utils.platform
import ru.DmN.siberia.processors.INodeProcessor
import ru.DmN.siberia.utils.VirtualType

object NRListOf : INodeProcessor<NodeNodesList> {
    override fun calc(node: NodeNodesList, processor: Processor, ctx: ProcessingContext): VirtualType =
        ctx.global.getType("List", processor.tp)

    override fun process(node: NodeNodesList, processor: Processor, ctx: ProcessingContext, mode: ValType): Node? =
        if (mode == VALUE)
            when (ctx.platform) {
                JAVA -> {
                    val info = node.info
                    NRMCall.process(
                        nodeMCall(
                            info,
                            nodeArrayOfType(info, "Any", node.nodes),
                            "toList",
                            emptyList()
                        ),
                        processor,
                        ctx,
                        VALUE
                    )
                }

                else -> node
            }
        else null
}