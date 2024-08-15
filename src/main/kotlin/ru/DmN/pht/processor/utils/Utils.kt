@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")
package ru.DmN.pht.processor.utils

import ru.DmN.pht.ast.NodeGetOrName
import ru.DmN.pht.module.utils.Module
import ru.DmN.pht.processors.IAdaptableProcessor
import ru.DmN.pht.processors.IComputableProcessor
import ru.DmN.pht.processors.IInlinableProcessor
import ru.DmN.pht.processors.ISetterGetterProcessor
import ru.DmN.pht.utils.lenArgs
import ru.DmN.pht.utils.node.nodeAs
import ru.DmN.siberia.ast.INodesList
import ru.DmN.siberia.ast.Node
import ru.DmN.siberia.ast.NodeNodesList
import ru.DmN.siberia.processor.Processor
import ru.DmN.siberia.processor.ctx.ProcessingContext
import ru.DmN.siberia.processors.INodeProcessor
import ru.DmN.siberia.utils.mapMutable
import ru.DmN.siberia.utils.vtype.VirtualMethod
import ru.DmN.siberia.utils.vtype.VirtualType
import ru.DmN.siberia.utils.vtype.VirtualType.Companion.VOID

inline fun <T> Processor.getOrCompute(node: Node, ctx: ProcessingContext, block: (Node, INodeProcessor<Node>) -> T): T {
    val np = get(node, ctx)
    return if (np is IComputableProcessor<Node>) {
        val new = np.compute(node, this, ctx)
        if (new == node)
            block(new, np)
        else block(new, get(new, ctx))
    } else block(node, np)
}

fun Processor.processAsSetterLazy(node: Node, value: List<Node>, ctx: ProcessingContext, valMode: Boolean): Node? =
    getOrCompute(node, ctx) { it, np ->
        if (np is ISetterGetterProcessor<Node>)
            np.processAsSetterLazy(it, value, this, ctx, valMode)
        else throw UnsupportedOperationException(np.javaClass.name)
    }

fun Processor.processAsSetter(node: Node, value: List<Node>, ctx: ProcessingContext, valMode: Boolean): Node? =
    getOrCompute(node, ctx) { it, np ->
        if (np is ISetterGetterProcessor<Node>)
            np.processAsSetter(it, value, this, ctx, valMode)
        else throw UnsupportedOperationException(np.javaClass.name)
    }

fun Processor.processAsGetter(node: Node, ctx: ProcessingContext): Node? =
    getOrCompute(node, ctx) { it, np ->
        if (np is ISetterGetterProcessor<Node>)
            np.processAsGetter(it, this, ctx)
        else throw UnsupportedOperationException(np.javaClass.name)
    }


fun Processor.processAndCast(node: Node, target: VirtualType, ctx: ProcessingContext, valMode: Boolean): Node? =
    process(if (target == VOID) node else nodeAs(node.info, node, target.name), ctx, valMode)

fun Processor.inline(node: NodeGetOrName, ctx: ProcessingContext): Pair<List<String>, Node> =
    getOrCompute(node, ctx) { it, np ->
        if (np is IInlinableProcessor<Node>)
            np.inline(it, this, ctx)
        else throw UnsupportedOperationException(np.javaClass.name)
    }

inline fun <T : Node?> Processor.inline(node: Node, other: T, ctx: ProcessingContext): Pair<List<String>, T> =
    getOrCompute(node, ctx) { it, np ->
        if (np is IInlinableProcessor<Node> && np.isInlinable(it, this, ctx))
            np.inline(it, this, ctx) as Pair<List<String>, T>
        else Pair(emptyList(), other)
    }

fun Processor.processNodes(node: INodesList, ctx: ProcessingContext, valMode: Boolean): MutableList<Node> =
    node.nodes.mapMutable { process(it, ctx, valMode)!! }

fun Processor.computeStringNodes(node: INodesList, ctx: ProcessingContext): List<String> =
    node.nodes.map { computeString(it, ctx) }

fun Processor.compute(node: Node, ctx: ProcessingContext): Node =
    getOrCompute(node, ctx) { it, np ->
        if (np is IComputableProcessor<Node>)
            np.compute(it, this, ctx)
        else it
    }

fun Processor.computeList(node: Node, ctx: ProcessingContext): List<Node> =
    computeListOr(node, ctx)!!

fun Processor.computeListOr(node: Node, ctx: ProcessingContext): List<Node>? =
    getOrCompute(node, ctx) { it, np ->
        if (np is IComputableProcessor<Node>)
            np.computeList(it, this, ctx)
        else null
    }

fun Processor.computeType(node: Node, ctx: ProcessingContext): VirtualType =
    computeTypeOr(node, ctx)!!

fun Processor.computeTypeOr(node: Node, ctx: ProcessingContext): VirtualType? =
    getOrCompute(node, ctx) { it, np ->
        if (np is IComputableProcessor<Node>)
            np.computeType(it, this, ctx)
        else null
    }

fun Processor.computeTypesOr(node: Node, ctx: ProcessingContext): List<VirtualType>? =
    getOrCompute(node, ctx) { it, np ->
        if (np is IComputableProcessor<Node>)
            np.computeTypes(it, this, ctx)
        else null
    }

fun Processor.computeGenericType(node: Node, ctx: ProcessingContext): String? =
    getOrCompute(node, ctx) { it, np ->
        if (np is IComputableProcessor<Node>)
            np.computeGenericType(it, this, ctx)
        else null
    }

fun Processor.computeString(node: Node, ctx: ProcessingContext): String =
    computeStringOr(node, ctx)!!

fun Processor.computeStringOr(node: Node, ctx: ProcessingContext): String? =
    getOrCompute(node, ctx) { it, np ->
        if (np is IComputableProcessor<Node>)
            np.computeString(it, this, ctx)
        else throw UnsupportedOperationException(it.javaClass.name)
    }

fun Processor.computeInt(node: Node, ctx: ProcessingContext): Int =
    computeIntOr(node, ctx)!!

fun Processor.computeIntOr(node: Node, ctx: ProcessingContext): Int? =
    getOrCompute(node, ctx) { it, np ->
        if (np is IComputableProcessor<Node>)
            np.computeInt(it, this, ctx)
        else throw UnsupportedOperationException(it.javaClass.name)
    }

fun Processor.adaptToType(node: Node, type: VirtualType, ctx: ProcessingContext): Node =
    getOrCompute(node, ctx) { it, np ->
        if (np is IAdaptableProcessor<Node>)
            np.adaptToType(it, type, this, ctx)
        else it
    }

/**
 * Возвращает обработчик нод из прошлых (вышестоящих) модулей.
 */
fun Processor.getOther(node: Node, ctx: ProcessingContext, module: Module): INodeProcessor<Node> {
    val iter = ctx.loadedModules.iterator()
    while (iter.hasNext())
        if (iter.next() == module)
            break
    val type = node.info.type
    if (iter.hasNext())
        iter.forEach { it -> it.processors[type]?.let { return it as INodeProcessor<Node> } }
    throw RuntimeException("Processor for \"$type\" not founded!")
}

fun getMethodVariants(variants: Sequence<VirtualMethod>, args: List<ICastable>): Sequence<Pair<VirtualMethod, Boolean>> =
    variants
        .map { Pair(it, if (it.modifiers.extension) listOf(ICastable.of(it.extension!!)) + args else args) }
        .filter { it.first.argsc.size == it.second.size || it.first.modifiers.varargs }
        .map { Pair(it.first, lenArgs(it.first.argsc, it.second, it.first.modifiers.varargs)) }
        .filter { it.second.first > -1 }
        .sortedBy { it.second.first }
        .map { Pair(it.first, it.second.second) }

fun Sequence<Node>.processValues(processor: Processor, ctx: ProcessingContext): Sequence<Node> =
    this.map { processor.process(it, ctx, true)!! }
fun Sequence<Node>.computeValues(processor: Processor, ctx: ProcessingContext): Sequence<Node> =
    this.map { processor.compute(it, ctx) }

/**
 * Обрабатывает все под-ноды в режиме "VALUE".
 */
fun <T : NodeNodesList> processValue(node: T, processor: Processor, ctx: ProcessingContext): T {
    for (i in 0 until node.nodes.size)
        node.nodes[i] = processor.process(node.nodes[i], ctx, true)!!
    return node
}

/**
 * Обрабатывает все под-ноды в режиме "NO_VALUE".
 */
fun <T : NodeNodesList> processNoValue(node: T, processor: Processor, ctx: ProcessingContext): T {
    for (i in 0 until node.nodes.size)
        node.nodes[i] = processor.process(node.nodes[i], ctx, false)!!
    return node
}

inline fun <T> sliceInsert(list: MutableList<T>, index: Int, elements: List<T>) {
    list as MutableList<T?>
    val right = list.subList(index + 1, list.size).toList()
    for (i in list.size until elements.size + index + right.size)
        list.add(null)
    elements.forEachIndexed { i, it -> list[index + i] = it }
    right.forEachIndexed { i, it -> list[index + i + elements.size] = it }
}