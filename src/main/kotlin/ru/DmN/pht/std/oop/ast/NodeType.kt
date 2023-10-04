package ru.DmN.pht.std.oop.ast

import ru.DmN.pht.base.lexer.Token
import ru.DmN.pht.base.parser.ast.Node
import ru.DmN.pht.base.parser.ast.NodeNodesList
import ru.DmN.pht.base.utils.VirtualType
import ru.DmN.pht.base.utils.indent

class NodeType(tkOperation: Token, nodes: MutableList<Node>, val type: VirtualType) : NodeNodesList(tkOperation, nodes), IAbstractlyNode, IFinally, IOpenly {
    override var abstract: Boolean
        get() = type.abstract
        set(value) { type.abstract = value }
    override var final: Boolean
        get() = type.final
        set(value) { type.final = value }
    override var open: Boolean
        get() = !type.final
        set(value) { type.final = !value }

    override fun copy(): NodeType =
        NodeType(tkOperation, copyNodes(), type)

    override fun print(builder: StringBuilder, indent: Int): StringBuilder {
        builder.indent(indent).append('[').append(tkOperation.text).append(' ').append(type.name).append(" (")
        type.parents.forEachIndexed { i, it ->
            builder.append(it.name)
            if (i + 1 < type.parents.size) {
                builder.append(' ')
            }
        }
        return printNodes(builder.append(')'), indent).append(']')
    }
}