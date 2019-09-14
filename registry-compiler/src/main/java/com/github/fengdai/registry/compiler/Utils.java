package com.github.fengdai.registry.compiler;

import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

final class Utils {
  private Utils() {
    throw new AssertionError();
  }

  static TypeMirror inferSuperTypeArgument(TypeElement element, String superTypeName,
      boolean isSuperTypeInterface, int typeArgumentIndex) {
    return infer(new LinkedList<>(), 0, element.asType(), superTypeName, isSuperTypeInterface,
        typeArgumentIndex);
  }

  private static TypeMirror infer(List<DeclaredType> hierarchy, int deep, TypeMirror type,
      String superTypeName, boolean isSuperTypeInterface, int typeArgumentIndex) {
    DeclaredType declaredType = (DeclaredType) type;
    TypeElement typeElement = (TypeElement) declaredType.asElement();
    if (hierarchy.size() > deep + 1) {
      while (hierarchy.size() > deep + 1) {
        hierarchy.remove(hierarchy.size() - 1);
      }
      hierarchy.set(deep, declaredType);
    } else if (hierarchy.size() == deep) {
      hierarchy.add(declaredType);
    }
    if (typeElement.toString().contentEquals(superTypeName)) {
      // Found. Try to infer.
      return analyseHierarchy(hierarchy, typeArgumentIndex);
    } else {
      // Not found. Search implemented interfaces and super class.
      List<TypeMirror> superTypes = new LinkedList<>();
      if (isSuperTypeInterface) {
        superTypes.addAll(typeElement.getInterfaces());
      }
      TypeMirror superClassType = typeElement.getSuperclass();
      if (superClassType.getKind() == TypeKind.DECLARED) {
        superTypes.add(superClassType);
      }
      for (TypeMirror superType : superTypes) {
        TypeMirror typeArgument =
            infer(hierarchy, deep + 1, superType, superTypeName, isSuperTypeInterface,
                typeArgumentIndex);
        if (typeArgument != null) return typeArgument;
        hierarchy.remove(hierarchy.size() - 1);
      }
    }
    return null;
  }

  private static TypeMirror analyseHierarchy(List<DeclaredType> hierarchy, int typeArgumentIndex) {
    DeclaredType declaredType = hierarchy.remove(hierarchy.size() - 1);
    TypeMirror typeArgument = declaredType.getTypeArguments().get(typeArgumentIndex);
    if (typeArgument.getKind() == TypeKind.DECLARED) {
      return typeArgument;
    } else {
      declaredType = hierarchy.get(hierarchy.size() - 1);
      TypeElement element = (TypeElement) declaredType.asElement();
      List<? extends TypeParameterElement> typeParameterElements = element.getTypeParameters();
      for (int i = 0; i < typeParameterElements.size(); i++) {
        TypeParameterElement typeParameterElement = typeParameterElements.get(i);
        if (typeParameterElement.asType().toString().equals(typeArgument.toString())) {
          return analyseHierarchy(hierarchy, i);
        }
      }
      return null;
    }
  }
}
