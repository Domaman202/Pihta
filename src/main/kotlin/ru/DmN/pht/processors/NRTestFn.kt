package ru.DmN.pht.processors

import ru.DmN.pht.utils.Platforms.JVM
import ru.DmN.pht.utils.computeString
import ru.DmN.pht.utils.node.nodeCls
import ru.DmN.pht.utils.node.nodeDefn
import ru.DmN.pht.utils.node.nodeStatic
import ru.DmN.siberia.Processor
import ru.DmN.siberia.ast.Node
import ru.DmN.siberia.ast.NodeNodesList
import ru.DmN.siberia.processor.ctx.ProcessingContext
import ru.DmN.siberia.processor.utils.platform
import ru.DmN.siberia.processors.INodeProcessor

object NRTestFn : INodeProcessor<NodeNodesList> {
    override fun process(node: NodeNodesList, processor: Processor, ctx: ProcessingContext, valMode: Boolean): Node =
        when (ctx.platform) {
            JVM -> {
                val info = node.info
                NRClass.process(
                    nodeCls(
                        info,
                        "Test${processor.computeString(node.nodes[0], ctx)}",
                        "java.lang.Object",
                        nodeStatic(
                            info,
                            nodeDefn(
                                info,
                                "test",
                                "dynamic",
                                node.nodes.drop(1).toMutableList()
                            )
                        )
                    ),
                    processor,
                    ctx,
                    valMode
                )
            }

            else -> node
        }
}