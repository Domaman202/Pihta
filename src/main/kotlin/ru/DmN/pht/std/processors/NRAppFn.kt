package ru.DmN.pht.std.processors

import ru.DmN.siberia.Processor
import ru.DmN.siberia.ast.Node
import ru.DmN.siberia.ast.NodeNodesList
import ru.DmN.siberia.lexer.Token
import ru.DmN.siberia.processor.utils.Platform
import ru.DmN.siberia.processor.utils.ProcessingContext
import ru.DmN.siberia.processor.utils.ValType
import ru.DmN.siberia.processor.utils.platform
import ru.DmN.siberia.processors.INodeProcessor
import ru.DmN.pht.std.processor.utils.clazzOrNull
import ru.DmN.pht.std.processor.utils.nodeClass
import ru.DmN.pht.std.processor.utils.nodeDefn
import ru.DmN.pht.std.processor.utils.nodeProgn

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