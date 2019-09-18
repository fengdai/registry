package com.github.fengdai.registry.processor

import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror

fun TypeElement.typeArgumentOf(
  superTypeName: String,
  typeArgumentIndex: Int
): TypeMirror? = infer(
    mutableListOf(), 0, asType(), superTypeName, typeArgumentIndex
)

private fun infer(
  hierarchy: MutableList<DeclaredType>,
  deep: Int,
  type: TypeMirror,
  superTypeName: String,
  typeArgumentIndex: Int
): TypeMirror? {
  val declaredType = type as DeclaredType
  val typeElement = declaredType.asElement() as TypeElement
  if (hierarchy.size > deep + 1) {
    while (hierarchy.size > deep + 1) {
      hierarchy.removeAt(hierarchy.size - 1)
    }
    hierarchy[deep] = declaredType
  } else if (hierarchy.size == deep) {
    hierarchy.add(declaredType)
  }
  if (typeElement.toString().contentEquals(superTypeName)) {
    // Found. Try to infer.
    return analyseHierarchy(hierarchy, typeArgumentIndex)
  } else {
    // Not found. Search implemented interfaces and super class.
    val superTypes = mutableListOf<TypeMirror>()
    superTypes.addAll(typeElement.interfaces)
    val superClassType = typeElement.superclass
    if (superClassType.kind == TypeKind.DECLARED) {
      superTypes.add(superClassType)
    }
    for (superType in superTypes) {
      val typeArgument = infer(hierarchy, deep + 1, superType, superTypeName, typeArgumentIndex)
      if (typeArgument != null) return typeArgument
      hierarchy.removeAt(hierarchy.size - 1)
    }
  }
  return null
}

private fun analyseHierarchy(
  hierarchy: MutableList<DeclaredType>,
  typeArgumentIndex: Int
): TypeMirror? {
  var declaredType = hierarchy.removeAt(hierarchy.size - 1)
  val typeArgument = declaredType.typeArguments[typeArgumentIndex]
  if (typeArgument.kind == TypeKind.DECLARED) {
    return typeArgument
  } else {
    declaredType = hierarchy[hierarchy.size - 1]
    val element = declaredType.asElement() as TypeElement
    val typeParameterElements = element.typeParameters
    for (i in typeParameterElements.indices) {
      val typeParameterElement = typeParameterElements[i]
      if (typeParameterElement.asType().toString() == typeArgument.toString()) {
        return analyseHierarchy(hierarchy, i)
      }
    }
    return null
  }
}
