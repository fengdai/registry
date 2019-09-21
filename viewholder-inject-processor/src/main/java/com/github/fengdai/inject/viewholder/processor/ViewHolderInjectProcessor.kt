package com.github.fengdai.inject.viewholder.processor

import com.github.fengdai.inject.viewholder.Inflate
import com.github.fengdai.inject.viewholder.Parent
import com.github.fengdai.inject.viewholder.ViewHolderInject
import com.github.fengdai.inject.viewholder.processor.DependencyRequest.Provided
import com.github.fengdai.inject.viewholder.processor.internal.filterNotNullValues
import com.github.fengdai.inject.viewholder.processor.internal.findElementsAnnotatedWith
import com.github.fengdai.inject.viewholder.processor.internal.hasAnnotation
import com.github.fengdai.inject.viewholder.processor.internal.toTypeName
import com.google.auto.common.MoreElements.getAnnotationMirror
import com.google.auto.service.AutoService
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
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic.Kind

private val VIEWHOLDER_INJECT_PARAMETER_ANNOTATIONS = setOf(Inflate::class.java, Parent::class.java)

@AutoService(Processor::class)
@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.ISOLATING)
class ViewHolderInjectProcessor : AbstractProcessor() {
  override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()
  override fun getSupportedAnnotationTypes() = setOf(ViewHolderInject::class.java.canonicalName) +
      VIEWHOLDER_INJECT_PARAMETER_ANNOTATIONS.map { it.canonicalName }

  override fun init(env: ProcessingEnvironment) {
    super.init(env)
    elements = env.elementUtils
    types = env.typeUtils
    filer = env.filer
    messager = env.messager
    idScanner = IdScanner(env)
    viewHolderType = elements.getTypeElement("android.support.v7.widget.RecyclerView.ViewHolder").asType()
    viewType = elements.getTypeElement("android.view.View").asType()
    viewGroupType = elements.getTypeElement("android.view.ViewGroup").asType()
  }

  private lateinit var elements: Elements
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

    VIEWHOLDER_INJECT_PARAMETER_ANNOTATIONS.forEach { env.errorWrongUsage(it) }
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
      error("@ViewHolderInject-using types must be subtypes of Recycler.ViewHolder.", this)
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
    val dependencyRequests = mutableListOf<DependencyRequest>()
    for (parameter in targetConstructor.parameters) {
      val inflateAnnotation = parameter.getAnnotation(Inflate::class.java)
      if (inflateAnnotation != null) {
        if (!types.isAssignable(parameter.asType(), viewType)) {
          error("@Inflate-annotated parameter must be type or subtype of View.", parameter)
        }
        val layoutRes = idScanner.elementToId(
            parameter,
            getAnnotationMirror(parameter, Inflate::class.java).get(),
            inflateAnnotation.value
        )
        dependencyRequests.add(DependencyRequest.Inflate(parameter.asNamedKey(), layoutRes))
        continue
      }

      if (parameter.hasAnnotation<Parent>()) {
        if (!types.isSameType(parameter.asType(), viewGroupType)) {
          error("Type of @Parent-annotated parameter must be ViewGroup.", parameter)
        }
        dependencyRequests.add(DependencyRequest.Parent(parameter.asNamedKey()))
        continue
      }

      dependencyRequests.add(Provided(parameter.asNamedKey()))
    }
    val targetType = targetType.asType()
        .toTypeName()
    return ViewHolderInjection(targetType, dependencyRequests.toList())
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

  private fun <T : Annotation> RoundEnvironment.errorWrongUsage(annotation: Class<T>) {
    val name = "@${annotation.simpleName}"
    val assistedMethods = getElementsAnnotatedWith(annotation)
        .map { it.enclosingElement as ExecutableElement }
    // Error any non-constructor usage of T.
    assistedMethods
        .filterNot { it.simpleName.contentEquals("<init>") }
        .forEach {
          error("$name is only supported on constructor parameters", it)
        }
    // Error any constructor usage of T which lacks method annotations.
    assistedMethods
        .filter { it.simpleName.contentEquals("<init>") }
        .filter { it.annotationMirrors.isEmpty() }
        .forEach {
          error("$name parameter use requires @ViewHolderInject-annotated constructor", it)
        }
    // Error any constructor usage of T which also uses @Inject.
    assistedMethods
        .filter { it.simpleName.contentEquals("<init>") }
        .filter { it.hasAnnotation("javax.inject.Inject") }
        .forEach {
          error("$name parameter does not work with @Inject! Use @ViewHolderInject", it)
        }
  }

  private fun error(
    message: String,
    element: Element? = null
  ) {
    messager.printMessage(Kind.ERROR, message, element)
  }

  data class ViewHolderInjectElements(
    val targetType: TypeElement,
    val targetConstructor: ExecutableElement
  )
}
