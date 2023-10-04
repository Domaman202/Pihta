package ru.DmN.pht.std.oop.ast

import ru.DmN.pht.base.lexer.Token
import ru.DmN.pht.base.parser.ast.Node
import ru.DmN.pht.base.parser.ast.NodeNodesList
import ru.DmN.pht.base.utils.VirtualMethod
import ru.DmN.pht.base.utils.indent

class NodeMCall(tkOperation: Token, nodes: MutableList<Node>, val instance: Node, val method: VirtualMethod, val type: Type) : NodeNodesList(tkOperation, nodes) {
    override fun copy(): NodeMCall =
        NodeMCall(tkOperation, copyNodes(), instance, method, type)

    override fun print(builder: StringBuilder, indent: Int): StringBuilder {
        builder.indent(indent).append('[').append(tkOperation.text).append(" (").append(type).append(") ").append(method.name).append(method.desc).append('\n')
        instance.print(builder, indent + 1)
        return printNodes(builder, indent).append(']')
    }

    enum class Type {
        UNKNOWN,
        EXTEND,
        STATIC,
        VIRTUAL,
        SUPER
    }
}