package ru.DmN.pht.processors

import ru.DmN.pht.ast.NodeEField
import ru.DmN.pht.processor.ctx.EnumConstContext
import ru.DmN.pht.processor.utils.clazz
import ru.DmN.pht.processor.utils.enum
import ru.DmN.pht.utils.computeList
import ru.DmN.pht.utils.computeString
import ru.DmN.pht.utils.node.NodeTypes.EFLD_
import ru.DmN.siberia.processor.Processor
import ru.DmN.siberia.ast.Node
import ru.DmN.siberia.ast.NodeNodesList
import ru.DmN.siberia.processor.ctx.ProcessingContext
import ru.DmN.siberia.processors.INodeProcessor
import ru.DmN.siberia.utils.vtype.FieldModifiers
import ru.DmN.siberia.utils.vtype.VirtualField.VirtualFieldImpl
import ru.DmN.siberia.utils.vtype.VirtualType

object NREFld : INodeProcessor<NodeNodesList> {
    override fun process(node: NodeNodesList, processor: Processor, ctx: ProcessingContext, valMode: Boolean): NodeEField {
        val type = ctx.clazz as VirtualType.VirtualTypeImpl
        val fields = type.fields
        val enums = ctx.enum.enums
        val list = ArrayList<Pair<String, List<Node>>>()
        processor.computeList(node.nodes[0], ctx).map { processor.computeList(it, ctx) }.forEach {
            val name = processor.computeString(it[0], ctx)
            val nodes = it.drop(1)
            list.add(Pair(name, nodes))
            fields += VirtualFieldImpl(type, name, type, FieldModifiers(isFinal = true, isStatic = true, isEnum = true))
            enums += EnumConstContext(name, nodes)
        }
        return NodeEField(node.info.withType(EFLD_), list)
    }
}