package com.github.fengdai.registry.compiler;

import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public class Utils {
  private Utils() {
    throw new AssertionError();
  }

  static TypeElement inferInterfaceTypeArgument(TypeElement element, String interfaceClassName,
      int typeArgumentIndex) {
    return infer(new LinkedList<DeclaredType>(), 0, element.asType(), interfaceClassName,
        typeArgumentIndex);
  }

  private static TypeElement infer(List<DeclaredType> hierarchy, int deep, TypeMirror type,
      String interfaceClassName, int typeArgumentIndex) {
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
    if (declaredType.asElement().toString().contentEquals(interfaceClassName)) {
      // Found. Try to infer.
      return analyseHierarchy(hierarchy, typeArgumentIndex);
    } else {
      // Not found. Search implemented interfaces and super class.
      for (TypeMirror superType : typeElement.getInterfaces()) {
        TypeElement typeArgument =
            infer(hierarchy, deep + 1, superType, interfaceClassName, typeArgumentIndex);
        if (typeArgument != null) return typeArgument;
      }
      TypeMirror superType = typeElement.getSuperclass();
      if (superType.getKind() == TypeKind.DECLARED) {
        TypeElement typeArgument =
            infer(hierarchy, deep + 1, superType, interfaceClassName, typeArgumentIndex);
        if (typeArgument != null) return typeArgument;
      }
    }
    return null;
  }

  private static TypeElement analyseHierarchy(List<DeclaredType> hierarchy, int typeArgumentIndex) {
    DeclaredType declaredType = hierarchy.remove(hierarchy.size() - 1);
    TypeMirror typeArgument = declaredType.getTypeArguments().get(typeArgumentIndex);
    if (typeArgument.getKind() == TypeKind.DECLARED) {
      return (TypeElement) ((DeclaredType) typeArgument).asElement();
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
