package com.github.fengdai.registry.processor

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.sun.tools.javac.code.Symbol

private val ANDROID_R = ClassName.get("android", "R")
private const val R = "R"

/**
 * Represents an ID of an Android resource.
 */
class Id @JvmOverloads constructor(
  val value: Int,
  rSymbol: Symbol? = null
) {
  val resourceName: String?
  val code: CodeBlock
  val qualifed: Boolean

  init {
    if (rSymbol != null) {
      val className = ClassName.get(
          rSymbol.packge().qualifiedName.toString(), R,
          rSymbol.enclClass().name.toString()
      )
      resourceName = rSymbol.name.toString()

      this.code = if (className.topLevelClassName() == ANDROID_R)
        CodeBlock.of("\$L.\$N", className, resourceName)
      else
        CodeBlock.of("\$T.\$N", className, resourceName)
      this.qualifed = true
    } else {
      resourceName = null
      this.code = CodeBlock.of("\$L", value)
      this.qualifed = false
    }
  }

  override fun equals(other: Any?): Boolean {
    return other is Id && value == other.value
  }

  override fun hashCode(): Int {
    return value
  }

  override fun toString(): String {
    throw UnsupportedOperationException("Please use value or code explicitly")
  }
}
