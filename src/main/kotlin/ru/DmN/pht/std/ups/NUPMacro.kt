package ru.DmN.pht.std.ups

import ru.DmN.siberia.Parser
import ru.DmN.siberia.Processor
import ru.DmN.siberia.Unparser
import ru.DmN.siberia.lexer.Token
import ru.DmN.siberia.parser.ctx.ParsingContext
import ru.DmN.siberia.ast.Node
import ru.DmN.siberia.ast.NodeNodesList
import ru.DmN.siberia.parsers.NPDefault
import ru.DmN.siberia.processor.utils.ProcessingContext
import ru.DmN.siberia.processors.NRDefault
import ru.DmN.siberia.processor.utils.ValType
import ru.DmN.siberia.unparser.UnparsingContext
import ru.DmN.siberia.unparsers.NUDefault
import ru.DmN.siberia.utils.VirtualType
import ru.DmN.siberia.utils.nextOperation
import ru.DmN.pht.std.processor.ctx.MacroContext
import ru.DmN.pht.std.processors.INodeUniversalProcessor
import ru.DmN.pht.std.ast.NodeMacro
import ru.DmN.pht.std.compiler.java.utils.SubMap
import ru.DmN.pht.std.processor.utils.*
import ru.DmN.pht.std.utils.text
import java.util.UUID

object NUPMacro : INodeUniversalProcessor<NodeMacro, NodeMacro> {
    override fun parse(parser: Parser, ctx: ParsingContext, token: Token): Node {
        val name = parser.nextOperation().text!!
        return NPDefault.parse(parser, ctx) { NodeMacro(token, it, name) }
    }

    override fun unparse(node: NodeMacro, unparser: Unparser, ctx: UnparsingContext, indent: Int) {
        unparser.out.apply {
            append('(').append(node.text).append(' ').append(node.name)
            NUDefault.unparseNodes(node, unparser, ctx, indent)
            append(')')
        }
    }

    override fun calc(node: NodeMacro, processor: Processor, ctx: ProcessingContext): VirtualType? {
        val result = macroCalc(node, ctx)
        return NRDefault.calc(result.first, processor, result.second)
    }

    override fun process(node: NodeMacro, processor: Processor, ctx: ProcessingContext, mode: ValType): Node {
        val result = macroCalc(node, ctx)
        return NRDefault.process(result.first.copy(), processor, result.second, mode)
    }

    private fun macroCalc(node: NodeMacro, ctx: ProcessingContext): Pair<NodeNodesList, ProcessingContext> {
        val gctx = ctx.global
        //
        val macro = gctx.macros.find { it.name == node.name } ?: throw RuntimeException("Macro '${node.name}' not founded!")
        val args = HashMap<Pair<UUID, String>, Node>()
        //
        if (macro.args.size == node.nodes.size)
            macro.args.forEachIndexed { i, it -> args[Pair(macro.uuid, it)] = node.nodes[i] }
        else if (macro.args.isNotEmpty()) {
            macro.args.dropLast(1).forEachIndexed { i, it -> args[Pair(macro.uuid, it)] = node.nodes[i] }
            args[Pair(macro.uuid, macro.args.last())] = NodeNodesList(
                Token(node.token.line, Token.Type.OPERATION, "valn"),
                node.nodes.drop(args.size - 1).toMutableList() // todo:
            )
        }
        //
        return Pair(
            nodeProgn(node.token.line, macro.body.toMutableList()),
            ctx.with(macro.ctx.combineWith(gctx)).with(macroCtxOf(ctx, args))
        )
    }

    fun macroCtxOf(ctx: ProcessingContext, args: MutableMap<Pair<UUID, String>, Node>): MacroContext {
        return if (ctx.isMacro())
            MacroContext(SubMap(ctx.macro.args, args))
        else MacroContext(args)
    }
}