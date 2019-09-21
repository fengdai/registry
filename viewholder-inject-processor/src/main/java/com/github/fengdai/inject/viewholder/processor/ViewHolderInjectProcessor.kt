package com.github.fengdai.inject.viewholder.processor

import com.github.fengdai.inject.viewholder.Inflate
import com.github.fengdai.inject.viewholder.NotProvided
import com.github.fengdai.inject.viewholder.Parent
import com.github.fengdai.inject.viewholder.ViewHolderInject
import com.github.fengdai.inject.viewholder.processor.Dependency.Request
import com.google.auto.common.MoreElements
import com.google.auto.service.AutoService
import com.google.common.collect.ImmutableList
import com.squareup.inject.assisted.processor.asNamedKey
import com.squareup.inject.assisted.processor.internal.filterNotNullValues
import com.squareup.inject.assisted.processor.internal.findElementsAnnotatedWith
import com.squareup.inject.assisted.processor.internal.hasAnnotation
import com.squareup.inject.assisted.processor.internal.toClassName
import com.squareup.javapoet.JavaFile
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind.CLASS
import javax.lang.model.element.ElementKind.CONSTRUCTOR
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.STATIC
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Types
import javax.tools.Diagnostic.Kind.ERROR

private const val VIEW_HOLDER = "android.support.v7.widget.RecyclerView.ViewHolder"
private const val VIEW = "android.view.View"
private const val VIEW_GROUP = "android.view.ViewGroup"

@AutoService(Processor::class)
@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.ISOLATING)
class ViewHolderInjectProcessor : AbstractProcessor() {
  override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()
  override fun getSupportedAnnotationTypes() = setOf(
      ViewHolderInject::class.java.canonicalName,
      Inflate::class.java.canonicalName,
      Parent::class.java.canonicalName
  )

  override fun init(env: ProcessingEnvironment) {
    super.init(env)
    types = env.typeUtils
    filer = env.filer
    messager = env.messager
    idScanner = IdScanner(env)
    val elements = env.elementUtils
    viewHolderType = elements.getTypeElement(VIEW_HOLDER)
        .asType()
    viewType = elements.getTypeElement(VIEW)
        .asType()
    viewGroupType = elements.getTypeElement(VIEW_GROUP)
        .asType()
  }

  private lateinit var types: Types
  private lateinit var filer: Filer
  private lateinit var messager: Messager
  private lateinit var idScanner: IdScanner
  private lateinit var viewHolderType: TypeMirror
  private lateinit var viewType: TypeMirror
  private lateinit var viewGroupType: TypeMirror

  override fun process(
    annotations: Set<TypeElement>,
    env: RoundEnvironment
  ): Boolean {
    env.findViewHolderInjectCandidateTypeElements()
        .asSequence()
        .mapNotNull { it.toViewHolderInjectElementsOrNull() }
        .associateWith { it.toViewHolderInjectionOrNull() }
        .filterNotNullValues()
        .forEach(::writeViewHolderInjection)
    return false
  }

  private fun RoundEnvironment.findViewHolderInjectCandidateTypeElements(): List<TypeElement> {
    return findElementsAnnotatedWith<ViewHolderInject>().map { it.enclosingElement as TypeElement }
  }

  private fun TypeElement.toViewHolderInjectElementsOrNull(): ViewHolderInjectElements? {
    var valid = true

    if (PRIVATE in modifiers) {
      error("@ViewHolderInject-using types must not be private", this)
      valid = false
    }

    if (enclosingElement.kind == CLASS && STATIC !in modifiers) {
      error("Nested @ViewHolderInject-using types must be static", this)
      valid = false
    }

    if (!types.isSubtype(asType(), viewHolderType)) {
      error("@ViewHolderInject-using types must be subtypes of $VIEW_HOLDER.", this)
      valid = false
    }

    val constructors = enclosedElements
        .filter { it.kind == CONSTRUCTOR }
        .filter { it.getAnnotation(ViewHolderInject::class.java) != null }
        .map { it as ExecutableElement }
    if (constructors.size > 1) {
      error("Multiple @ViewHolderInject-annotated constructors found.", this)
      valid = false
    }

    if (!valid) return null

    val constructor = constructors.single()
    if (PRIVATE in constructor.modifiers) {
      error("@ViewHolderInject constructor must not be private.", constructor)
      return null
    }

    return ViewHolderInjectElements(this, constructor)
  }

  private fun ViewHolderInjectElements.toViewHolderInjectionOrNull(): ViewHolderInjection? {
    val dependencies = ImmutableList.Builder<Dependency>()
    for (parameter in targetConstructor.parameters) {
      val inflateAnnotation = parameter.getAnnotation(Inflate::class.java)
      if (inflateAnnotation != null) {
        val viewType = parameter.asType()
        if (!types.isAssignable(viewType, this@ViewHolderInjectProcessor.viewType)) {
          error("@Inflate-annotated parameter must be $VIEW or subtype of $VIEW.", parameter)
        }
        val layoutRes = idScanner.elementToId(
            parameter,
            MoreElements.getAnnotationMirror(parameter, Inflate::class.java).get(),
            inflateAnnotation.value
        )
        dependencies.add(Dependency.Inflate(parameter.asNamedKey(), layoutRes))
        continue
      }

      if (parameter.hasAnnotation<Parent>()) {
        if (!types.isSameType(parameter.asType(), viewGroupType)) {
          error("Type of @Parent-annotated parameter must be $VIEW_GROUP.", parameter)
        }
        dependencies.add(Dependency.Parent(parameter.asNamedKey()))
        continue
      }

      dependencies.add(Request(parameter.asNamedKey(), parameter.hasAnnotation<NotProvided>()))
    }
    return ViewHolderInjection(targetType.toClassName(), dependencies.build())
  }

  private fun writeViewHolderInjection(
    elements: ViewHolderInjectElements,
    injection: ViewHolderInjection
  ) {
    val generatedTypeSpec = injection.brewJava()
        .toBuilder()
        .addOriginatingElement(elements.targetType)
        .build()
    JavaFile.builder(injection.generatedType.packageName(), generatedTypeSpec)
        .addFileComment("Generated by @ViewHolderInject. Do not modify!")
        .build()
        .writeTo(filer)
  }

  private fun error(
    message: String,
    element: Element? = null
  ) {
    messager.printMessage(ERROR, message, element)
  }

  data class ViewHolderInjectElements(
    val targetType: TypeElement,
    val targetConstructor: ExecutableElement
  )
}
