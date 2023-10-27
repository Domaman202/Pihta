package ru.DmN.pht.std.ups

import ru.DmN.pht.base.Parser
import ru.DmN.pht.base.Processor
import ru.DmN.pht.base.Unparser
import ru.DmN.pht.base.lexer.Token
import ru.DmN.pht.base.parser.ParsingContext
import ru.DmN.pht.base.ast.Node
import ru.DmN.pht.base.ast.NodeNodesList
import ru.DmN.pht.base.parsers.NPDefault
import ru.DmN.pht.base.processor.ProcessingContext
import ru.DmN.pht.base.processor.ValType
import ru.DmN.pht.base.unparser.UnparsingContext
import ru.DmN.pht.base.utils.VirtualField
import ru.DmN.pht.std.processor.utils.clazz
import ru.DmN.pht.std.processor.utils.global
import ru.DmN.pht.std.processors.INodeUniversalProcessor
import ru.DmN.pht.std.utils.computeList
import ru.DmN.pht.std.utils.computeString
import ru.DmN.pht.std.ast.NodeField

object NUPField : INodeUniversalProcessor<NodeField, NodeNodesList> {
    override fun parse(parser: Parser, ctx: ParsingContext, operationToken: Token): Node? =
        NPDefault.parse(parser, ctx, operationToken)

    override fun unparse(node: NodeField, unparser: Unparser, ctx: UnparsingContext, indent: Int) {
        unparser.out.apply {
            append('(').append(node.token.text).append(' ').append('[')
            node.fields.forEach {
                append("[^").append(it.first).append(' ').append(it.second).append(']')
            }
            append("])")
        }
    }

    override fun process(node: NodeNodesList, processor: Processor, ctx: ProcessingContext, mode: ValType): Node {
        val gctx = ctx.global
        val clazz = ctx.clazz
        val list = ArrayList<Pair<String, String>>()
        processor.computeList(node.nodes[0], ctx).map { it -> processor.computeList(it, ctx).map { processor.computeString(it, ctx) } }.forEach {
            val name = it[0]
            val type = it[1]
            list.add(Pair(type, name))
            clazz.fields += VirtualField(clazz, name, gctx.getType(type, processor.tp), static = false, enum = false)
        }
        return NodeField(node.token, list)
    }
}