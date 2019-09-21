package com.github.fengdai.inject.viewholder.processor

import com.sun.source.util.Trees
import com.sun.tools.javac.code.Symbol
import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.tree.TreeScanner
import java.util.Objects.requireNonNull
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element

class IdScanner(processingEnv: ProcessingEnvironment) {
  private var trees: Trees? = null
  private val rScanner = RScanner()

  init {
    try {
      trees = Trees.instance(processingEnv)
    } catch (ignored: IllegalArgumentException) {
      try {
        // Get original ProcessingEnvironment from Gradle-wrapped one or KAPT-wrapped one.
        for (field in processingEnv.javaClass.declaredFields) {
          if (field.name == "delegate" || field.name == "processingEnv") {
            field.isAccessible = true
            val javacEnv = field.get(processingEnv) as ProcessingEnvironment
            trees = Trees.instance(javacEnv)
            break
          }
        }
      } catch (ignored2: Throwable) {
      }
    }
  }

  fun elementToId(
    element: Element,
    annotationMirror: AnnotationMirror,
    value: Int
  ): Id {
    val tree = trees?.getTree(element, annotationMirror) as? JCTree
    if (tree != null) { // tree can be null if the references are compiled types and not source
      rScanner.reset()
      tree.accept(rScanner)
      if (rScanner.resourceIds.isNotEmpty()) {
        return rScanner.resourceIds.values.iterator()
            .next()
      }
    }
    return Id(value)
  }

  fun elementToIds(
    element: Element,
    annotationMirror: AnnotationMirror,
    values: Iterable<Int>
  ): Map<Int, Id> {
    var resourceIds = mutableMapOf<Int, Id>()
    val tree = trees?.getTree(element, annotationMirror) as? JCTree
    if (tree != null) { // tree can be null if the references are compiled types and not source
      rScanner.reset()
      tree.accept(rScanner)
      resourceIds = rScanner.resourceIds
    }

    // Every value looked up should have an Id
    for (value in values) {
      resourceIds.putIfAbsent(value, Id(value))
    }
    return resourceIds
  }

  private class RScanner : TreeScanner() {
    val resourceIds = mutableMapOf<Int, Id>()

    override fun visitSelect(jcFieldAccess: JCTree.JCFieldAccess) {
      val symbol = jcFieldAccess.sym
      if (symbol.enclosingElement != null
          && symbol.enclosingElement.enclosingElement != null
          && symbol.enclosingElement.enclosingElement.enclClass() != null
      ) {
        try {
          val value = requireNonNull((symbol as Symbol.VarSymbol).constantValue) as Int
          resourceIds[value] = Id(value, symbol)
        } catch (ignored: Exception) {
        }
      }
    }

    override fun visitLiteral(jcLiteral: JCTree.JCLiteral?) {
      try {
        val value = jcLiteral!!.value as Int
        resourceIds[value] = Id(value)
      } catch (ignored: Exception) {
      }
    }

    fun reset() {
      resourceIds.clear()
    }
  }
}
