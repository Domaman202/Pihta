package ru.DmN.pht.jvm.utils.vtype

import ru.DmN.pht.utils.vtype.*
import ru.DmN.siberia.utils.Klass
import ru.DmN.siberia.utils.klassOf
import ru.DmN.siberia.utils.vtype.*
import java.lang.reflect.*
import java.util.*

/**
 * Java Runtime Types Provider
 *
 * Предоставляет типы путём получения классов из ClassLoader-а.
 */
class JRTP : TypesProvider() {
    override fun typeOf(name: String): VirtualType =
        types[name.hashCode()]
            ?: try {
                addType(klassOf(name))
            } catch (e: Exception) {
                if (name.startsWith("ru."))
                    println()
                throw ClassNotFoundException("Класс '$name' не найден!", e)
            }

    override fun typeOfOrNull(name: String): VirtualType? =
        try { typeOf(name) } catch (_: ClassNotFoundException) { null }

    private fun typeOf(klass: Klass): VirtualType =
        typeOfOrNull(klass.name) ?: addType(klass)

    private fun addType(klass: Klass): VirtualType =
        if (klass.isArray)
            VVTArray(PhtVirtualType.of(typeOf(klass.componentType)))
        else JavaVirtualTypeImpl(
            klass.name,
            klass.componentType?.let(::typeOf),
            klass.isInterface,
            klass.interfaces.any { it.name == "groovy.lang.GroovyObject" },
            Modifier.isFinal(klass.modifiers) || klass.isEnum
        ).apply {
            this@JRTP += this
            //
            klass.superclass?.let { parents.add(typeOf(it.name)) }
            Arrays.stream(klass.interfaces).map { typeOf(it.name) }.forEach(parents::add)
            //
            klass.typeParameters.forEach {
                val bound = it.bounds.lastOrNull()
                val generic = "${it.name}$$name"
                genericsAccept += generic
                genericsDefine[generic] = typeOf(if (bound is Klass) bound else Any::class.java)
            }
            klass.genericSuperclass.let {
                if (it is ParameterizedType) {
                    it.actualTypeArguments.forEachIndexed { j, it1 ->
                        val type = klass.superclass
                        genericsMap["${type.typeParameters[j].name}$${type.name}"] = "${it1.typeName}$$name"
                    }
                }
            }
            klass.genericInterfaces.forEachIndexed { i, it0 ->
                if (it0 is ParameterizedType) {
                    it0.actualTypeArguments.forEachIndexed { j, it1 ->
                        val type = klass.interfaces[i]
                        genericsMap["${type.typeParameters[j].name}$${type.name}"] = "${it1.typeName}$$name"
                    }
                }
            }
            parents.forEach { it0 ->
                it0.genericsMap.forEach { it1 ->
                    genericsMap[it1.value]?.let { genericsMap[it1.key] = it }
                }
            }
            //
            klass.declaredFields.forEach { fields += VirtualField.of(::typeOf, it) }
            klass.declaredConstructors.forEach { methods += methodOf(::typeOf, it) }
            scanTypeMethods(methods, klass)
        }

    private fun scanTypeMethods(list: MutableList<VirtualMethod>, klass: Klass) {
        klass.declaredMethods.forEach { list += methodOf(::typeOf, it) }
        if (klass.superclass == null)
            return
        scanTypeMethods(list, klass.superclass)
        klass.interfaces.forEach { scanTypeMethods(list, it) }
    }

    /**
     * Создаёт новый метод.
     * Использует typeOf метод для взятия новых типов по имени.
     */
    private fun methodOf(typeOf: (name: String) -> VirtualType, ctor: Constructor<*>): VirtualMethod =
        methodOf(typeOf(ctor.declaringClass.name), ctor)

    /**
     * Создаёт новый метод.
     * Использует typeOf метод для взятия новых типов по имени.
     */
    private fun methodOf(typeOf: (name: String) -> VirtualType, method: Method): VirtualMethod =
        methodOf(typeOf(method.declaringClass.name), method)

    /**
     * Создаёт новый метод.
     */
    private fun methodOf(declaringClass: VirtualType, method: Constructor<*>): VirtualMethod {
        val argsc = ArrayList<VirtualType>()
        val argsn = ArrayList<String>()
        val argsg = ArrayList<String?>()
        method.parameters.forEach {
            argsc += VirtualType.ofKlass(it.type)
            argsn += it.name
//            argsg += it.parameterizedType.let { if (it is TypeVariable<*>) "${it.name}$${decl}" else null } // А зачем?
        }
        return VirtualMethod.Impl(
            declaringClass,
            "<init>",
            VirtualType.VOID,
            null,
            argsc,
            argsn,
            argsg,
            MethodModifiers(
                varargs = method.isVarArgs,
                static = Modifier.isStatic(method.modifiers),
                abstract = method.declaringClass.isInterface,
                final = Modifier.isFinal(method.modifiers)
            ),
            null,
            null
        )
    }

    private fun fakeTypeOf(name: String): VirtualType =
        if (name.startsWith('['))
            fakeTypeOf(name.substring(1)).arrayType
        else if (name == "dynamic")
            VTDynamic
        else typeOf(name)

    /**
     * Создаёт новый метод.
     */
    private fun methodOf(declaringClass: VirtualType, method: Method): VirtualMethod {
        val isDynRet = method.isAnnotationPresent(DynamicReturn::class.java)
        val isDynArgs = method.isAnnotationPresent(DynamicArguments::class.java)
        val fakeArgTypes =
            if (method.isAnnotationPresent(FakeArgumentType::class.java)) {
                val map = HashMap<Int, VirtualType>()
                method.getAnnotationsByType(FakeArgumentType::class.java)
                    .forEach { map[it.position] = fakeTypeOf(it.type) }
                map
            } else null
        //
        val argsc = ArrayList<VirtualType>()
        val argsn = ArrayList<String>()
        val argsg = ArrayList<String?>()
        val generics = declaringClass.genericsDefine.toMutableMap()
        val decl = declaringClass.name
        if (isDynArgs) {
            method.parameters.forEach { it ->
                argsc += VTDynamic
                argsn += it.name
                argsg += it.parameterizedType.let { if (it is TypeVariable<*>) "${it.name}$$decl" else null }
            }
        } else if (fakeArgTypes != null) {
            method.parameters.forEachIndexed { i, it ->
                argsc += fakeArgTypes[i] ?: typeOf(it.type)
                argsn += it.name
                argsg += it.parameterizedType.let { if (it is TypeVariable<*>) "${it.name}$$decl" else null }
            }
        } else {
            method.parameters.forEach { it ->
                argsc += typeOf(it.type)
                argsn += it.name
                argsg += it.parameterizedType.let { if (it is TypeVariable<*>) "${it.name}$$decl" else null }
            }
        }
        // todo: add method only generics
        return PhtVirtualMethod.Impl(
            declaringClass,
            method.name,
            if (isDynRet) VTDynamic else typeOf(method.returnType),
            method.genericReturnType.let { if (it is TypeVariable<*>) "${it.name}$$decl" else null },
            argsc,
            argsn,
            argsg,
            MethodModifiers(
                varargs = method.isVarArgs,
                static = Modifier.isStatic(method.modifiers),
                abstract = Modifier.isAbstract(method.modifiers),
                final = Modifier.isFinal(method.modifiers)
            ),
            extension = null,
            generator = null,
            inline = null,
            generics
        )
    }
}