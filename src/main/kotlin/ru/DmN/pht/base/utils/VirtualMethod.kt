package ru.DmN.pht.base.utils

import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.lang.reflect.Modifier

data class VirtualMethod(
    var declaringClass: VirtualType?,
    var generics: Generics,
    var name: String,
    var rettype: TypeOrGeneric,
    var argsc: List<TypeOrGeneric>,
    var argsn: List<String>,
    val modifiers: MethodModifiers,
    var extend: VirtualType? = null
) {
    val argsDesc: String
        get() {
            val str = StringBuilder()
            argsc.forEach { str.append(it.type.desc) }
            return str.toString()
        }
    val desc: String
        get() = "($argsDesc)${if (name.startsWith("<")) "V" else rettype.type.desc}"

    companion object {
        fun of(typeOf: (name: String) -> VirtualType, ctor: Constructor<*>): VirtualMethod =
            of(typeOf(ctor.declaringClass.name), ctor)
        fun of(typeOf: (name: String) -> VirtualType, method: Method): VirtualMethod =
            of(typeOf(method.declaringClass.name), method)
        fun of(ctor: Constructor<*>): VirtualMethod =
            of(VirtualType.ofKlass(ctor.declaringClass), ctor)
        fun of(method: Method): VirtualMethod =
            of(VirtualType.ofKlass(method.declaringClass), method)

        private fun of(declaringClass: VirtualType, method: Constructor<*>): VirtualMethod {
            val generics = Generics()
            val argsc = ArrayList<TypeOrGeneric>()
            val argsn = ArrayList<String>()
            if (declaringClass.final) {
                method.parameters.forEach {
                    argsc += TypeOrGeneric.of(generics, it.type)
                    argsn += it.name
                }
            } else {
                val gpt = method.genericParameterTypes
                method.parameters.forEachIndexed { i, it ->
                    argsc += TypeOrGeneric.of(generics, gpt[i])
                    argsn += it.name
                }
            }
            return VirtualMethod(
                declaringClass,
                generics,
                "<init>",
                TypeOrGeneric.of(generics, VirtualType.VOID),
                argsc,
                argsn,
                MethodModifiers(
                    varargs = method.isVarArgs,
                    static = Modifier.isStatic(method.modifiers),
                    abstract = method.declaringClass.isInterface
                ),
                null
            )
        }

        private fun of(declaringClass: VirtualType, method: Method): VirtualMethod {
            val generics = Generics()
            val argsc = ArrayList<TypeOrGeneric>()
            val argsn = ArrayList<String>()
            if (declaringClass.final) {
                method.parameters.forEach {
                    argsc += TypeOrGeneric.of(generics, it.type)
                    argsn += it.name
                }
            } else {
                val gpt = method.genericParameterTypes
                method.parameters.forEachIndexed { i, it ->
                    argsc += TypeOrGeneric.of(generics, gpt[i])
                    argsn += it.name
                }
            }
            return VirtualMethod(
                declaringClass,
                generics,
                method.name,
                TypeOrGeneric.of(generics, method.genericReturnType),
                argsc,
                argsn,
                MethodModifiers(
                    varargs = method.isVarArgs,
                    static = Modifier.isStatic(method.modifiers),
                    abstract = method.declaringClass.isInterface
                ),
                null
            )
        }
    }
}