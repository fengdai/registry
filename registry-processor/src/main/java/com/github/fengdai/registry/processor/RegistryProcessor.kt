package com.github.fengdai.registry.processor

import com.github.fengdai.inject.viewholder.processor.IdScanner
import com.github.fengdai.registry.processor.internal.castEach
import com.github.fengdai.registry.processor.internal.getAnnotation
import com.github.fengdai.registry.processor.internal.toClassName
import com.github.fengdai.registry.processor.internal.toTypeName
import com.google.auto.service.AutoService
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeName
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic.Kind.ERROR

private const val REGISTER_TYPE = "com.github.fengdai.registry.Register"
private const val BINDER_VIEW_HOLDER = "com.github.fengdai.registry.BinderViewHolder"
private const val BINDER = "com.github.fengdai.registry.Binder"

@AutoService(Processor::class)
@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.ISOLATING)
class RegistryProcessor : AbstractProcessor() {
  override fun getSupportedAnnotationTypes(): Set<String> = setOf(REGISTER_TYPE)
  override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

  override fun init(env: ProcessingEnvironment) {
    super.init(env)
    elements = env.elementUtils
    types = env.typeUtils
    filer = env.filer
    idScanner = IdScanner(processingEnv)
  }

  private lateinit var elements: Elements
  private lateinit var types: Types
  private lateinit var filer: Filer
  private lateinit var idScanner: IdScanner

  override fun process(
    annotations: Set<TypeElement>,
    env: RoundEnvironment
  ): Boolean {
    env.findRegistryCandidateTypeElements()
        .associateWith { it.toRegistryClass() }
        .forEach(::writeRegistry)
    return false
  }

  private fun RoundEnvironment.findRegistryCandidateTypeElements(): List<TypeElement> {
    return getElementsAnnotatedWith(elements.getTypeElement(REGISTER_TYPE)).castEach()
  }

  private fun TypeElement.toRegistryClass(): RegistryClass {
    val registerAnnotation =
      RegisterAnnotation(getAnnotation(REGISTER_TYPE)!!)

    val viewTypes = mutableMapOf<TypeName, Int>()
    val builders = mutableMapOf<ClassName, BindingSet.Builder>()

    // TODO validation
    (registerAnnotation.binders.map {
      it.binderToBinding(viewTypes)
    } + registerAnnotation.binderViewHolders.map {
      it.binderViewHolderToBinding(viewTypes)
    }).forEach {
      builders.addBinding(it, this, registerAnnotation.annotation)
    }

    val staticContentLayouts = idScanner.elementToIds(
        this,
        registerAnnotation.annotation,
        registerAnnotation.staticContentLayoutsValues
    )
    val public = PUBLIC in modifiers
    return RegistryClass(
        toClassName(),
        public,
        bindingSets = builders.values.map { it.build() },
        indexedViewHolderTypes = viewTypes.map { Pair(it.value, it.key) },
        staticContentLayouts = staticContentLayouts.values.toList()
    )
  }

  private fun MutableMap<ClassName, BindingSet.Builder>.addBinding(
    binding: Binding,
    annotationElement: Element,
    annotationMirror: AnnotationMirror
  ) {
    try {
      getOrPut(binding.dataRawType, { BindingSet.Builder(binding.dataRawType) }).add(binding)
    } catch (e: DuplicateBindingException) {
      processingEnv.messager.printMessage(ERROR, e.message, annotationElement, annotationMirror)
    }
  }

  private fun MutableMap<TypeName, Int>.getViewType(viewHolderType: TypeName): Int =
    getOrPut(viewHolderType, { size })

  private fun TypeElement.binderToBinding(viewTypes: MutableMap<TypeName, Int>): Binding {
    val binderType = toClassName()
    val dataType = typeArgumentOf(BINDER, 0)!!.toTypeName()
    val viewHolderType = typeArgumentOf(BINDER, 1)!!.toTypeName()
    return Binding(binderType, true, dataType, viewHolderType, viewTypes.getViewType(viewHolderType))
  }

  private fun TypeElement.binderViewHolderToBinding(viewTypes: MutableMap<TypeName, Int>): Binding {
    val dataType = typeArgumentOf(BINDER_VIEW_HOLDER, 0)!!.toTypeName()
    val viewHolderType = toClassName()
    return Binding(viewHolderType, false, dataType, viewHolderType, viewTypes.getViewType(viewHolderType))
  }

  private fun writeRegistry(
    element: Element,
    registryClass: RegistryClass
  ) {
    val generatedTypeSpec = registryClass.brewJava()
        .toBuilder()
        .addOriginatingElement(element)
        .build()
    JavaFile.builder(registryClass.generatedType.packageName(), generatedTypeSpec)
        .addFileComment("Generated by @Register. Do not modify!")
        .build()
        .writeTo(filer)
  }
}
