package com.github.fengdai.registry.compiler;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import java.util.Collection;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import static com.google.auto.common.MoreElements.getPackage;

final class ViewHolderInfo {
  private static final String FACTORY_IMPL_SUFFIX = "_Factory";

  final TypeElement viewHolderElement;
  final ClassName viewHolderClassName;
  final Collection<Dependency> dependencies;
  final Collection<Dependency> unresolvedDependencies;
  final ClassName factoryImplClassName;

  ViewHolderInfo(TypeElement viewHolderElement, Collection<Dependency> dependencies) {
    this.viewHolderElement = viewHolderElement;
    this.viewHolderClassName = ClassName.get(viewHolderElement);
    this.dependencies = dependencies;
    this.unresolvedDependencies = dependencies.stream()
        .filter(it -> !it.canBeResolved)
        .collect(Collectors.toList());
    String classPackage = getPackage(viewHolderElement).getQualifiedName().toString();
    String className = viewHolderElement.getQualifiedName()
        .toString()
        .substring(classPackage.length() + 1)
        .replace('.', '$') + FACTORY_IMPL_SUFFIX;
    this.factoryImplClassName = ClassName.get(classPackage, className);
  }

  static class Dependency {
    private static final ClassName UTILS =
        ClassName.get("com.github.fengdai.registry.internal", "Utils");
    private static final ClassName RECYCLER_VIEW =
        ClassName.get("android.support.v7.widget", "RecyclerView");

    final TypeName typeName;
    final ParameterSpec parameterSpec;
    final @Nullable InflateLayout inflateLayout;

    final CodeBlock instanceCode;
    final boolean canBeResolved;

    Dependency(VariableElement element, @Nullable InflateLayout inflateLayout) {
      this.typeName = TypeName.get(element.asType());
      this.parameterSpec = ParameterSpec.get(element).toBuilder()
          .addAnnotations(element.getAnnotationMirrors()
              .stream()
              .map(AnnotationSpec::get)
              .collect(Collectors.toList()))
          .build();
      this.inflateLayout = inflateLayout;

      if (inflateLayout != null) {
        instanceCode = CodeBlock.of("($T) $T.inflate(parent, $L)",
            inflateLayout.viewClassName, UTILS, inflateLayout.layoutRes.code);
        canBeResolved = true;
      } else if (typeName.equals(RECYCLER_VIEW)) {
        instanceCode = CodeBlock.of("($T) parent", RECYCLER_VIEW);
        canBeResolved = true;
      } else {
        instanceCode = CodeBlock.of("$N", parameterSpec);
        canBeResolved = false;
      }
    }
  }
}
