package ru.DmN.pht.processors

import ru.DmN.pht.processor.utils.computeString
import ru.DmN.pht.utils.Platforms.CPP
import ru.DmN.pht.utils.Platforms.JVM
import ru.DmN.pht.utils.dropMutable
import ru.DmN.pht.utils.node.*
import ru.DmN.siberia.ast.Node
import ru.DmN.siberia.ast.NodeNodesList
import ru.DmN.siberia.processor.Processor
import ru.DmN.siberia.processor.ctx.ProcessingContext
import ru.DmN.siberia.processor.utils.nodeProgn
import ru.DmN.siberia.processor.utils.platform
import ru.DmN.siberia.processors.INodeProcessor

object NRTestFn : INodeProcessor<NodeNodesList> {
    override fun process(node: NodeNodesList, processor: Processor, ctx: ProcessingContext, valMode: Boolean): Node? =
        when (val platform = ctx.platform) {
            JVM, CPP -> {
                val info = node.info
                processor.process(
                    nodeTest(
                        info,
                        nodeCls(
                            info,
                            "Test${processor.computeString(node.nodes[0], ctx)}",
                            "Object",
                            nodeStatic(
                                info,
                                nodeDefn(
                                    info,
                                    "test",
                                    if (platform == JVM) "dynamic" else "void",
                                    node.nodes.dropMutable(1).let {
                                        if (platform == CPP)
                                            mutableListOf(nodePrintln(info, mutableListOf(nodeProgn(info, it))))
                                        else it
                                    }
                                )
                            )
                        )
                    ),
                    ctx,
                    false
                )
            }

            else -> node
        }
}