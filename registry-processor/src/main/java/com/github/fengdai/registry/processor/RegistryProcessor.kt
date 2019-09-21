package com.github.fengdai.registry.processor

import com.github.fengdai.registry.Binder
import com.github.fengdai.registry.BinderViewHolder
import com.github.fengdai.registry.Register
import com.github.fengdai.registry.processor.internal.castEach
import com.github.fengdai.registry.processor.internal.filterNotNullValues
import com.github.fengdai.registry.processor.internal.findElementsAnnotatedWith
import com.github.fengdai.registry.processor.internal.getAnnotation
import com.github.fengdai.registry.processor.internal.toClassName
import com.github.fengdai.registry.processor.internal.toTypeName
import com.google.auto.common.AnnotationMirrors.getAnnotationValue
import com.google.auto.common.MoreTypes.asTypeElement
import com.google.auto.service.AutoService
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeName
import com.sun.tools.javac.code.Attribute
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic.Kind.ERROR

private val BINDER_VIEW_HOLDER = BinderViewHolder::class.java.canonicalName
private val BINDER = Binder::class.java.canonicalName

@AutoService(Processor::class)
@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.ISOLATING)
class RegistryProcessor : AbstractProcessor() {
  override fun getSupportedAnnotationTypes(): Set<String> =
    setOf(Register::class.java.canonicalName)

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
        .mapNotNull { it.toRegistryElementsOrNull() }
        .associateWith { it.toRegistryClassOrNull() }
        .filterNotNullValues()
        .forEach(::writeRegistry)
    return false
  }

  private fun RoundEnvironment.findRegistryCandidateTypeElements(): List<TypeElement> {
    return findElementsAnnotatedWith<Register>().castEach()
  }

  private fun TypeElement.toRegistryElementsOrNull(): RegistryElements? {
    val registerAnnotation = getAnnotation<Register>()!!

    val binders = (getAnnotationValue(registerAnnotation, "binders") as Attribute.Array).value
        .filterIsInstance<Attribute.Class>()
        .map { asTypeElement(it.value) }
        .toSet()

    val binderViewHolders =
      (getAnnotationValue(registerAnnotation, "binderViewHolders") as Attribute.Array).value
          .filterIsInstance<Attribute.Class>()
          .map { asTypeElement(it.value) }
          .toSet()

    val staticContentLayoutsValue =
      (getAnnotationValue(registerAnnotation, "staticContentLayouts") as Attribute.Array).value
    val staticContentLayouts = staticContentLayoutsValue.filterIsInstance<Attribute.Constant>()
        .mapNotNull { it.value as? Int }
        .toSet()

    return RegistryElements(this, binders, binderViewHolders, staticContentLayouts)
  }

  private fun RegistryElements.toRegistryClassOrNull(): RegistryClass? {
    val registerAnnotation = targetType.getAnnotation<Register>()!!

    val viewTypes = mutableMapOf<TypeName, Int>()
    val builders = mutableMapOf<ClassName, BindingSet.Builder>()

    val binderBindings = binders.map { it.binderToBinding(viewTypes) }
    val binderViewHolderBindings = binderViewHolders.map { it.binderViewHolderToBinding(viewTypes) }
    (binderBindings + binderViewHolderBindings).forEach { binding ->
      try {
        builders.getOrPut(binding.dataRawType, { BindingSet.Builder(binding.dataRawType) }).add(binding)
      } catch (e: DuplicateBindingException) {
        processingEnv.messager.printMessage(ERROR, e.message, targetType, registerAnnotation)
        return null
      }
    }

    val registeredAnnotation = targetType.toClassName()
    val public = PUBLIC in targetType.modifiers
    val bindingSets = builders.values.map { it.build() }
    val indexedViewHolderTypes = viewTypes.map { Pair(it.value, it.key) }
    val staticContentLayouts =
      idScanner.elementToIds(targetType, registerAnnotation, staticContentLayouts).values.toList()
    return RegistryClass(
        registeredAnnotation, public, bindingSets, indexedViewHolderTypes, staticContentLayouts)
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
    elements: RegistryElements,
    registryClass: RegistryClass
  ) {
    val generatedTypeSpec = registryClass.brewJava()
        .toBuilder()
        .addOriginatingElement(elements.targetType)
        .build()
    JavaFile.builder(registryClass.generatedType.packageName(), generatedTypeSpec)
        .addFileComment("Generated by @Register. Do not modify!")
        .build()
        .writeTo(filer)
  }

  private data class RegistryElements(
    val targetType: TypeElement,
    val binders: Set<TypeElement>,
    val binderViewHolders: Set<TypeElement>,
    val staticContentLayouts: Set<Int>
  )
}
