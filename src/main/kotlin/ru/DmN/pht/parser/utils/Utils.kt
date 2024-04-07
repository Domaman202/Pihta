@file:Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
package ru.DmN.pht.parser.utils

import ru.DmN.pht.utils.ctx.ContextKeys
import ru.DmN.siberia.lexer.Token
import ru.DmN.siberia.parser.Parser
import ru.DmN.siberia.parser.ctx.ParsingContext
import ru.DmN.siberia.utils.ctx.IContextCollection
import java.util.*


inline fun Parser.parseMacro(ctx: ParsingContext, token: Token) =
    get(ctx, "macro")!!.parse(this, ctx, token)
inline fun Parser.parseMCall(ctx: ParsingContext, token: Token) =
    get(ctx, "mcall!")!!.parse(this, ctx, token)

var IContextCollection<*>.macros
    set(value) { this.contexts[ContextKeys.MACROS] = value }
    get() = this.contexts[ContextKeys.MACROS] as Stack<UUID>