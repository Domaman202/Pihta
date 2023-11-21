package ru.DmN.pht.std.ups

import ru.DmN.pht.base.Parser
import ru.DmN.pht.base.Processor
import ru.DmN.pht.base.Unparser
import ru.DmN.pht.base.ast.Node
import ru.DmN.pht.base.lexer.Token
import ru.DmN.pht.base.parser.ctx.ParsingContext
import ru.DmN.pht.base.parsers.NPDefault
import ru.DmN.pht.base.processor.utils.Platform
import ru.DmN.pht.base.processor.utils.ProcessingContext
import ru.DmN.pht.base.processor.utils.ProcessingStage
import ru.DmN.pht.base.processor.utils.ValType
import ru.DmN.pht.base.unparser.UnparsingContext
import ru.DmN.pht.base.utils.nextOperation
import ru.DmN.pht.base.utils.platform
import ru.DmN.pht.std.ast.NodeImport
import ru.DmN.pht.std.imports.StdImportsHelper
import ru.DmN.pht.std.imports.ast.IValueNode
import ru.DmN.pht.std.processor.utils.global
import ru.DmN.pht.std.processor.utils.macros
import ru.DmN.pht.std.processors.INodeUniversalProcessor
import ru.DmN.pht.std.utils.text

object NUPImport : INodeUniversalProcessor<NodeImport, NodeImport> {
    override fun parse(parser: Parser, ctx: ParsingContext, token: Token): Node {
        val module = parser.nextOperation().text!!
        val context = ctx.subCtx()
        context.loadedModules.add(0, StdImportsHelper)
        return NPDefault.parse(parser, context) { it ->
            val map = HashMap<String, MutableList<Any?>>()
            it.forEach { map.getOrPut(it.text) { ArrayList() } += (it as IValueNode).value }
            NodeImport(token, module, map)
        }
    }

    override fun unparse(node: NodeImport, unparser: Unparser, ctx: UnparsingContext, indent: Int) {
        unparser.out.apply {
            append('(').append(node.text).append(' ').append(node.module)
            node.data.forEach {
                append('\n').append("\t".repeat(indent + 1))
                    .append('(').append(it.key).append(' ').append(it.value).append(')')
            }
            append(')')
        }
    }

    override fun process(node: NodeImport, processor: Processor, ctx: ProcessingContext, mode: ValType): NodeImport? {
        val gctx = ctx.global

        processor.pushTask(ctx, ProcessingStage.MACROS_IMPORT) {
            node.data["macros"]?.run {
                val cmacros = gctx.macros
                val pmacros = processor.contexts.macros
                forEach { it ->
                    it as String
                    val i = it.lastIndexOf('.')
                    val name = it.substring(i + 1)
                    cmacros += pmacros[it.substring(0, i)]!!.find { it.name == name }!!
                }
            }
        }

        processor.pushTask(ctx, ProcessingStage.TYPES_IMPORT) {
            node.data["types"]?.run {
                val imports = gctx.imports
                forEach {
                    it as String
                    imports[it.substring(it.lastIndexOf('.') + 1)] = it
                }
            }
        }

        processor.pushTask(ctx, ProcessingStage.EXTENDS_IMPORT) {
            node.data["extends"]?.forEach { it ->
                it as String
                gctx.getType(it, processor.tp).methods
                    .stream()
                    .filter { it.modifiers.extend }
                    .forEach { ctx.global.getExtends(it.extend!!) += it }
            }
        }

        return if (ctx.platform == Platform.JAVA)
            null
        else node
    }
}