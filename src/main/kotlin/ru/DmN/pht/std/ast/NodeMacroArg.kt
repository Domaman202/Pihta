package ru.DmN.pht.std.ast

import ru.DmN.pht.base.lexer.Token
import ru.DmN.pht.base.ast.Node
import ru.DmN.pht.base.ast.NodeNodesList
import ru.DmN.pht.base.utils.indent
import java.util.UUID

class NodeMacroArg(tkOperation: Token, nodes: MutableList<Node>, val uuids: List<UUID>) : NodeNodesList(tkOperation, nodes) {
    override fun copy(): NodeMacroArg =
        NodeMacroArg(token, copyNodes(), uuids)

    override fun print(builder: StringBuilder, indent: Int): StringBuilder {
        builder.indent(indent).append('[').append(token.text).append(" (").append(uuids).append(')')
        return printNodes(builder, indent).append(']')
    }
}