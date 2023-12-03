package ru.DmN.phtx.pls.processor.utils

import com.kingmang.lazurite.parser.ast.*
import ru.DmN.pht.std.ast.NodeModifierNodesList
import ru.DmN.pht.std.ast.NodeValue
import ru.DmN.pht.std.processor.utils.nodeDefn
import ru.DmN.pht.std.processor.utils.nodeGetOrName
import ru.DmN.pht.std.processor.utils.nodeMCall
import ru.DmN.pht.std.processor.utils.nodeValueOf
import ru.DmN.siberia.ast.Node
import ru.DmN.siberia.ast.NodeNodesList
import ru.DmN.siberia.lexer.Token

fun nodePrognB(line: Int, nodes: MutableList<Node>) =
    NodeModifierNodesList(Token.operation(line, "progn-"), nodes)

fun convert(line: Int, stmt: com.kingmang.lazurite.parser.ast.Node): Node =
    when (stmt) {
        is MStatement -> nodePrognB(line, stmt.statements.asSequence().map { convert(line, it) }.toMutableList())
        is ExprStatement -> convert(line, stmt.expr)
        is PrintStatement -> NodeNodesList(
            Token.operation(line, "println"),
            mutableListOf(convert(line, stmt.expression))
        )

        is ValueExpression -> when (stmt.value.type()) {
            1 -> nodeValueOf(line, stmt.value.asInt())
            2 -> nodeValueOf(line, stmt.value.asString())
            else -> throw UnsupportedOperationException()
        }

        is BinaryExpression -> NodeNodesList(
            Token.operation(
                line, when (stmt.operation.toString()) {
                    "+" -> "add"
                    "-" -> "sub"
                    "*" -> "mul"
                    "/" -> "div"
                    "%" -> "rem"
                    "&" -> "and"
                    "|" -> "or"
                    "^" -> "xor"
                    "<< " -> "shift-left"
                    ">>" -> "shift-right"
                    else -> throw UnsupportedOperationException()
                }
            ),
            mutableListOf(convert(line, stmt.expr1), convert(line, stmt.expr2))
        )

        is AssignmentExpression -> NodeNodesList(
            Token.operation(line, "def-set"),
            mutableListOf(convert(line, stmt.target), convert(line, stmt.expression))
        )

        is VariableExpression -> when (stmt.name) {
            "true" -> nodeValueOf(line, true)
            "false" -> nodeValueOf(line, false)
            else -> nodeGetOrName(line, stmt.name)
        }

        is FunctionDefineStatement -> nodeDefn(
            line,
            stmt.name,
            "dynamic",
            stmt.arguments.map {
                if (it.valueExpr == null) Pair(it.name, "dynamic") else Pair(
                    it.name,
                    (it.valueExpr as VariableExpression).name
                )
            },
            mutableListOf(convert(line, stmt.body))
        )

        is ReturnStatement -> NodeNodesList(
            Token.operation(line, "ret"),
            mutableListOf(convert(line, stmt.expression))
        )
        is FunctionalExpression -> nodeMCall(
            line,
            nodeGetOrName(line, "."),
            (stmt.functionExpr as VariableExpression).name,
            stmt.arguments.map { convert(line, it) }
        )

        is IfStatement -> NodeNodesList(
            Token.operation(line, "if"),
            sequenceOf(stmt.expression, stmt.ifStatement, stmt.elseStatement)
                .filterNotNull()
                .map { convert(line, it) }
                .toMutableList()
        )

        else -> throw UnsupportedOperationException()
    }