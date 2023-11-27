package ru.DmN.siberia.compiler.java.utils

import ru.DmN.siberia.processor.utils.Platform
import ru.DmN.siberia.processor.utils.with
import ru.DmN.siberia.processor.utils.withJCV
import ru.DmN.siberia.utils.IContextCollection
import ru.DmN.siberia.utils.Module
import ru.DmN.siberia.utils.getJavaClassVersion
import ru.DmN.pht.std.compiler.java.utils.SubList
import ru.DmN.pht.std.compiler.java.utils.SubMap
import java.util.concurrent.atomic.AtomicReference

class CompilationContext(
    val stage: AtomicReference<CompilingStage>,
    val loadedModules: MutableList<Module> = ArrayList(),
    override val contexts: MutableMap<String, Any?> = HashMap()) : IContextCollection<CompilationContext> {
    fun subCtx() =
        CompilationContext(stage, SubList(loadedModules), SubMap(contexts))
    override fun with(name: String, ctx: Any?): CompilationContext =
        CompilationContext(stage, loadedModules, contexts.toMutableMap().apply { this[name] = ctx })

    companion object {
        fun base() =
            CompilationContext(AtomicReference(CompilingStage.UNKNOWN), mutableListOf(ru.DmN.siberia.Siberia)).with(Platform.JAVA).withJCV(getJavaClassVersion())
    }
}