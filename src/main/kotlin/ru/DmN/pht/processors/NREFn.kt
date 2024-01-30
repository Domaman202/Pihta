package ru.DmN.pht.processors

import ru.DmN.pht.ast.NodeInlBodyB
import ru.DmN.pht.ast.NodeDefn
import ru.DmN.pht.node.NodeTypes
import ru.DmN.pht.processor.ctx.BodyContext
import ru.DmN.pht.processor.utils.clazz
import ru.DmN.pht.processor.utils.global
import ru.DmN.pht.processor.utils.with
import ru.DmN.pht.utils.*
import ru.DmN.siberia.Processor
import ru.DmN.siberia.ast.NodeNodesList
import ru.DmN.siberia.processor.ctx.ProcessingContext
import ru.DmN.siberia.processor.utils.ProcessingStage
import ru.DmN.siberia.processor.utils.ValType
import ru.DmN.siberia.processor.utils.processNodesList
import ru.DmN.siberia.processors.INodeProcessor
import ru.DmN.siberia.utils.MethodModifiers
import ru.DmN.siberia.utils.VirtualMethod
import ru.DmN.siberia.utils.VirtualType

object NREFn : INodeProcessor<NodeNodesList> {
    override fun process(node: NodeNodesList, processor: Processor, ctx: ProcessingContext, mode: ValType): NodeDefn {
        val gctx = ctx.global
        val type = ctx.clazz as VirtualType.VirtualTypeImpl
        //
        val gens = processor.computeListOr(node.nodes[0], ctx)
        val offset = if (gens == null) 0 else 1
        val generics = type.generics.toMutableMap()
        gens?.forEach {
            val generic = processor.computeList(it, ctx)
            generics[processor.computeString(generic[0], ctx)] = processor.computeType(generic[1], ctx)
        }
        //
        val extend = processor.computeType(node.nodes[offset], ctx)
        val name = processor.computeString(node.nodes[1 + offset], ctx)
        val returnGen = processor.computeGenericType(node.nodes[2 + offset], ctx)
        val returnType =
            if (returnGen == null)
                processor.computeType(node.nodes[2 + offset], ctx)
            else generics[returnGen]!!
        val args = NRDefn.parseArguments(node.nodes[3 + offset], generics, processor, ctx, gctx)
        //
        args.first.add(0, extend)
        args.second.add(0, "this")
        args.third.add(0, null)
        //
        val method = VirtualMethod.VirtualMethodImpl(
            type,
            name,
            returnType,
            returnGen,
            args.first,
            args.second,
            args.third,
            MethodModifiers(static = true, extension = true),
            extend,
            null,
            generics
        )
        type.methods += method
        gctx.getExtensions(extend) += method
        //
        val new = NodeDefn(node.info.withType(NodeTypes.EFN_), node.nodes.drop(4 + offset).toMutableList(), method)
        //
        processor.stageManager.pushTask(ProcessingStage.METHODS_BODY) {
            val context = ctx.with(method).with(BodyContext.of(method))
            if (method.modifiers.inline)
                method.inline = NodeInlBodyB(node.info.withType(NodeTypes.INL_BODY_A), new.nodes.toMutableList(), method.rettype, context)
            processNodesList(
                new,
                processor,
                context,
                if (method.rettype == VirtualType.VOID)
                    ValType.NO_VALUE
                else ValType.VALUE
            )
        }
        //
        return new
    }
}