package ru.DmN.pht.std.processors

import ru.DmN.pht.base.Processor
import ru.DmN.pht.base.ast.Node
import ru.DmN.pht.base.ast.NodeNodesList
import ru.DmN.pht.base.lexer.Token
import ru.DmN.pht.base.processor.Platform
import ru.DmN.pht.base.processor.ProcessingContext
import ru.DmN.pht.base.processor.ValType
import ru.DmN.pht.base.processors.INodeProcessor
import ru.DmN.pht.base.utils.platform
import ru.DmN.pht.std.processor.utils.*

object NRAppFn : INodeProcessor<NodeNodesList> {
    override fun process(node: NodeNodesList, processor: Processor, ctx: ProcessingContext, mode: ValType): Node =
        when (ctx.platform) {
            Platform.UNIVERSAL -> node
            Platform.JAVA -> {
                val line = node.token.line
                val fn = NodeNodesList(
                    Token.operation(line, "@static"), mutableListOf(
                        nodeDefn(
                            line,
                            "main",
                            "void",
                            emptyList(),
                            mutableListOf(nodeProgn(line, node.nodes))
                        )
                    )
                )
                if (ctx.clazzOrNull?.name == "App")
                    processor.process(fn, ctx, mode)!!
                else {
                    NRClass.process(
                        nodeClass(
                            line,
                            "App",
                            listOf("java.lang.Object"),
                            mutableListOf(fn)
                        ), processor, ctx, mode
                    )
                }
            }
        }
}