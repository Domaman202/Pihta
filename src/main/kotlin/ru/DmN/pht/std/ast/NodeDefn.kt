package ru.DmN.pht.std.ast

import ru.DmN.siberia.lexer.Token
import ru.DmN.siberia.ast.Node
import ru.DmN.siberia.ast.NodeNodesList
import ru.DmN.siberia.utils.VirtualMethod
import ru.DmN.siberia.utils.indent

class NodeDefn(
    tkOperation: Token,
    nodes: MutableList<Node>,
    val method: VirtualMethod
) : NodeNodesList(tkOperation, nodes), IAbstractlyNode, IStaticallyNode, IVarargNode {
    override var abstract: Boolean
        set(value) { method.modifiers.abstract = value }
        get() = method.modifiers.abstract
    override var static: Boolean
        set(value) { method.modifiers.static = value }
        get() = method.modifiers.static
    override var varargs: Boolean
        set(value) { method.modifiers.varargs = value }
        get() = method.modifiers.varargs

    override fun copy(): NodeDefn =
        NodeDefn(token, copyNodes(), method).apply { static = this@NodeDefn.static }

    override fun print(builder: StringBuilder, indent: Int): StringBuilder {
        builder.indent(indent).append('[').append(token.text).append(' ')
        return printNodes(if (method.modifiers.ctor) builder.append('(').append(method.argsDesc).append(')') else builder.append(method.name).append(method.desc), indent).append(']')
    }
}