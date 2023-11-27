package ru.DmN.siberia

import ru.DmN.siberia.ast.Node
import ru.DmN.siberia.lexer.Lexer
import ru.DmN.siberia.lexer.Token
import ru.DmN.siberia.lexer.Token.Type.CLOSE_BRACKET
import ru.DmN.siberia.parser.ctx.ParsingContext
import ru.DmN.siberia.parser.utils.baseParseNode
import ru.DmN.siberia.parsers.INodeParser
import ru.DmN.siberia.utils.getRegex
import java.util.*

class Parser(val lexer: Lexer, var parseNode: Parser.(ctx: ParsingContext) -> Node?) {
    val tokens = Stack<Token>()

    constructor(code: String) : this(Lexer(code), { baseParseNode(it) })

    fun parseNode(ctx: ParsingContext) =
        parseNode(this, ctx)

    fun get(ctx: ParsingContext, name: String): INodeParser? {
        val i = name.lastIndexOf('/')
        return if (i < 1) {
            ctx.loadedModules.forEach { it -> it.parsers.getRegex(name)?.let { return it } }
            null
        } else {
            val module = name.substring(0, i)
            ctx.loadedModules.find { it.name == module }?.parsers?.getRegex(name.substring(i + 1))
        }
    }

    inline fun <T> pnb(body: () -> T): T = body.invoke().apply { tryClose() }

    fun tryClose() {
        val token = nextToken()
        if (token?.type != CLOSE_BRACKET) {
            tokens.push(token)
        }
    }

    fun nextToken(): Token? {
        return if (tokens.empty())
            lexer.next()
        else tokens.pop()
    }
}