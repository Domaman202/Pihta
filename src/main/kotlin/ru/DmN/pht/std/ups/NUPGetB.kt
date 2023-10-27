package ru.DmN.pht.std.ups

import ru.DmN.pht.base.Parser
import ru.DmN.pht.base.ast.Node
import ru.DmN.pht.base.lexer.Token
import ru.DmN.pht.base.lexer.isNaming
import ru.DmN.pht.base.lexer.isOperation
import ru.DmN.pht.base.parser.ParsingContext
import ru.DmN.pht.std.ast.NodeFMGet
import ru.DmN.pht.std.ast.NodeGetOrName
import ru.DmN.pht.std.ast.NodeValue
import ru.DmN.pht.std.processors.INodeUniversalProcessor

object NUPGetB : INodeUniversalProcessor<Node, Node> {
    override fun parse(parser: Parser, ctx: ParsingContext, operationToken: Token): Node {
        val nameToken = parser.nextToken()!!
        return when (nameToken.type) {
            Token.Type.CLASS -> parse(
                operationToken,
                nameToken.text!!,
                static = true,
                klass = true
            )
            Token.Type.OPERATION -> parse(
                operationToken,
                nameToken.text!!,
                static = false,
                klass = false
            )
            Token.Type.OPEN_BRACKET -> {
                parser.tokens.push(nameToken)
                return NodeFMGet(
                    operationToken,
                    parser.parseNode(ctx)!!,
                    parser.nextToken()!!
                        .let { if (it.isOperation() || it.isNaming()) it else throw RuntimeException() }.text!!,
                    false
                )
            }

            else -> throw RuntimeException()
        }
    }

    private fun parse(operationToken: Token, name: String, static: Boolean, klass: Boolean): Node {
        val parts = name.split("/", "#") as MutableList<String>
        return parse(operationToken, parts, parts.size, static, klass)
    }

    private fun parse(operationToken: Token, parts: List<String>, i: Int, static: Boolean, clazz: Boolean): Node {
        val j = i - 1
        return if (j == 0) {
            if (clazz)
                NodeValue(Token(operationToken.line, Token.Type.OPERATION, "value"), NodeValue.Type.CLASS, parts[0])
            else NodeGetOrName(Token(operationToken.line, Token.Type.OPERATION, "get-or-name!"), parts[0], static)
        } else {
            val isStatic = static && j == 1
            NodeFMGet(
                Token(operationToken.line, Token.Type.OPERATION, "fget!",),
                parse(operationToken, parts, j, static, clazz),
                parts[j],
                isStatic
            )
        }
    }
}