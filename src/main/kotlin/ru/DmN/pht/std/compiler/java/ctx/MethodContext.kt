package ru.DmN.pht.std.compiler.java.ctx

import org.objectweb.asm.tree.MethodNode
import ru.DmN.siberia.utils.VirtualMethod

class MethodContext(val node: MethodNode, val method: VirtualMethod)