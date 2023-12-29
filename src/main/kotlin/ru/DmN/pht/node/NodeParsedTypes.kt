package ru.DmN.pht.std.node

import ru.DmN.pht.std.node.NodeTypes.*
import ru.DmN.siberia.node.INodeType

enum class NodeParsedTypes(override val operation: String, override val processed: INodeType) : IParsedNodeType {
    // a
    ADD("add", ADD_),
    AND("and", AND_),
    AS("as", AS_),
    ASET("aset", ASET_),
    // b
    BODY("body", BODY_),
    BREAK("break", BREAK_),
    // c
    CATCH("catch", CATCH_),
    CLS("cls", CLS_),
    CONTINUE("continue", CONTINUE_),
    CYCLE("cycle", CYCLE_),
    DEC_PRE("dec", DEC_PRE_),
    DEC_POST("dec-", DEC_POST_),
    DEFN("defn", DEFN_),
    DIV("div", DIV_),
    // e
    EFLD("efld", EFLD_),
    EFN("efn", EFN_),
    EQ("eq", EQ_),
    // f
    FGET_A("fget", FGET_),
    FGET_B("fget!", FGET_),
    FLD("fld", FLD_),
    FN("fn", FN_),
    FSET_A("fset", FSET_),
    FSET_B("fset!", FSET_),
    // g
    GET_B("get!", GET_),
    GREAT("great", GREAT_),
    GREAT_OR_EQ("great-or-eq", GREAT_OR_EQ_),
    // i
    IMPORT("import", IMPORT_),
    INC_PRE("inc", INC_PRE_),
    INC_POST("inc-", INC_POST_),
    IS("is", IS_),
    ITF("itf", ITF_),
    // l
    LESS("less", LESS_),
    LESS_OR_EQ("less-or-eq", LESS_OR_EQ_),
    // m
    MCALL("mcall", MCALL_),
    MUL("mul", MUL_),
    // n
    NAMED_BLOCK("named-block", NAMED_BLOCK_),
    NEG("neg", NEG_),
    NEW("new", NEW_),
    NEW_ARRAY("new-array", NEW_ARRAY_),
    NOT("not", NOT_),
    NOT_EQ("not-eq", NOT_EQ_),
    NS("ns", NS_),
    // o
    OBJ("obj", OBJ_),
    OR("or", OR_),
    // r
    REM("rem", REM_),
    RET("ret", RET_),
    // s
    SET_B("set!", SET_),
    SHIFT_LEFT("shift-left", SHIFT_LEFT_),
    SHIFT_RIGHT("shift-right", SHIFT_RIGHT_),
    SUB("sub", SUB_),
    // v
    VALN("valn", VALN_),
    // x
    XOR("xor", XOR_),

    // @
    ANN_ABSTRACT("@abstract", ANN_ABSTRACT_),
    ANN_FINAL("@final", ANN_FINAL_),
    ANN_OPEN("@open", ANN_OPEN_),
    ANN_STATIC("@static", ANN_STATIC_),
    ANN_VARARGS("@varargs", ANN_VARARGS_);

    override val processable: Boolean
        get() = true
    override val compilable: Boolean
        get() = false
}