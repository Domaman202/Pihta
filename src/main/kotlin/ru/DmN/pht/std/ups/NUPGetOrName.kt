package ru.DmN.pht.std.ups

import ru.DmN.siberia.Parser
import ru.DmN.siberia.Processor
import ru.DmN.siberia.Unparser
import ru.DmN.siberia.lexer.Token
import ru.DmN.siberia.parser.ctx.ParsingContext
import ru.DmN.siberia.ast.Node
import ru.DmN.siberia.processor.utils.ProcessingContext
import ru.DmN.siberia.unparser.UnparsingContext
import ru.DmN.siberia.utils.VirtualType
import ru.DmN.siberia.utils.nextOperation
import ru.DmN.pht.std.ast.NodeGetOrName
import ru.DmN.pht.std.processor.utils.body
import ru.DmN.pht.std.processor.utils.clazz
import ru.DmN.pht.std.processor.utils.isClass
import ru.DmN.siberia.utils.INUP
import ru.DmN.pht.std.processors.IStdNodeProcessor

object NUPGetOrName : INUP<NodeGetOrName, NodeGetOrName>, IStdNodeProcessor<NodeGetOrName> {
    override fun parse(parser: Parser, ctx: ParsingContext, token: Token): Node? {
        val tk = parser.nextOperation()
        return if (tk.text!!.contains("[/#]".toRegex())) {
            parser.tokens.push(tk)
            parser.get(ctx, "get")!!.parse(parser, ctx, Token(token.line, Token.Type.OPERATION, "get!"))
        } else NodeGetOrName(token, tk.text, false)
    }

    override fun unparse(node: NodeGetOrName, unparser: Unparser, ctx: UnparsingContext, indent: Int) {
        unparser.out.append(node.name)
    }

    override fun calc(node: NodeGetOrName, processor: Processor, ctx: ProcessingContext): VirtualType =
        if (node.name == "super")
            ctx.body["this"]!!.type()
        else {
            val variable = ctx.body[node.name]
            variable?.type() ?: if (ctx.isClass()) ctx.clazz.fields.find { it.name == node.name }!!.type else throw RuntimeException()
        }

    override fun computeString(node: NodeGetOrName, processor: Processor, ctx: ProcessingContext): String =
        node.getValueAsString()

    override fun computeInt(node: NodeGetOrName, processor: Processor, ctx: ProcessingContext): Int =
        node.getValueAsString().toInt()
}