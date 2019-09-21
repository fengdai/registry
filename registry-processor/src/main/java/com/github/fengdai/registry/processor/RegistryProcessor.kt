package com.github.fengdai.registry.processor

import com.github.fengdai.registry.processor.RegistryProcessor.InjectionType.INVALID
import com.github.fengdai.registry.processor.RegistryProcessor.InjectionType.JAVAX_INJECTION
import com.github.fengdai.registry.processor.RegistryProcessor.InjectionType.NONE
import com.github.fengdai.registry.processor.RegistryProcessor.InjectionType.VIEW_HOLDER_INJECTION
import com.github.fengdai.registry.processor.internal.MirrorValue
import com.github.fengdai.registry.processor.internal.cast
import com.github.fengdai.registry.processor.internal.castEach
import com.github.fengdai.registry.processor.internal.filterNotNullValues
import com.github.fengdai.registry.processor.internal.getAnnotation
import com.github.fengdai.registry.processor.internal.getValue
import com.github.fengdai.registry.processor.internal.hasAnnotation
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
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind.ANNOTATION_TYPE
import javax.lang.model.element.ElementKind.CLASS
import javax.lang.model.element.ElementKind.CONSTRUCTOR
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic.Kind.ERROR

private const val REGISTER = "com.github.fengdai.registry.Register"
private const val REGISTRY_MODULE = "com.github.fengdai.registry.RegistryModule"
private const val BINDER_VIEW_HOLDER = "com.github.fengdai.registry.BinderViewHolder"
private const val BINDER = "com.github.fengdai.registry.Binder"

@AutoService(Processor::class)
@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.ISOLATING)
class RegistryProcessor : AbstractProcessor() {
  override fun getSupportedAnnotationTypes(): Set<String> = setOf(
      REGISTER,
      REGISTRY_MODULE
  )

  override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

  override fun init(env: ProcessingEnvironment) {
    super.init(env)
    elements = env.elementUtils
    types = env.typeUtils
    filer = env.filer
    idScanner = IdScanner(processingEnv)

    registerType = elements.getTypeElement(REGISTER)
    registryModuleType = elements.getTypeElement(REGISTRY_MODULE)
  }

  private lateinit var elements: Elements
  private lateinit var types: Types
  private lateinit var filer: Filer
  private lateinit var idScanner: IdScanner

  private lateinit var registerType: TypeElement
  private lateinit var registryModuleType: TypeElement

  private val userModules = mutableListOf<String>()

  override fun process(
    annotations: Set<TypeElement>,
    env: RoundEnvironment
  ): Boolean {
    val registryElementsList = env.findRegistryCandidateTypeElements()
        .mapNotNull { it.toRegistryElementsOrNull() }

    registryElementsList
        .asSequence()
        .associateWith { it.toRegistryClassOrNull() }
        .filterNotNullValues()
        .forEach(::writeRegistry)

    val registryModuleElements = registryElementsList
        .filter { it.registryModuleElement != null }
        .mapNotNull { it.toRegistryModuleElementsOrNull() }

    registryModuleElements.associateWith { it.toRegistryInjectionModule() }
        .forEach(::writeRegistryModule)

    env.getElementsAnnotatedWith(registryModuleType)
        .filter { it.enclosingElement.kind != ANNOTATION_TYPE || !it.enclosingElement.hasAnnotation(REGISTER) }
        .forEach { registryModule ->
          error("@RegistryModule must be declared as a nested type of a @Register-annotated type.",
              registryModule)
        }

    userModules += registryModuleElements.map { it.registryModuleElement.qualifiedName.toString() }

    // Wait until processing is ending to validate that the @RegistryModule's @Module annotation
    // includes the generated type.
    if (userModules.isNotEmpty() && env.processingOver()) {
      userModules.forEach { userModuleFqcn ->
        // In the processing round in which we handle the @RegistryModule the @Module annotation's
        // includes contain an <error> type because we haven't generated the inflation module yet.
        // As a result, we need to re-lookup the element so that its referenced types are available.
        val userModule = elements.getTypeElement(userModuleFqcn)

        // Previous validation guarantees this annotation is present.
        val moduleAnnotation = userModule.getAnnotation("dagger.Module")!!
        // Dagger guarantees this property is present and is an array of types or errors.
        val includes = moduleAnnotation.getValue("includes", elements)!!
            .cast<MirrorValue.Array>()
            .filterIsInstance<MirrorValue.Type>()

        val generatedModuleName = (userModule.enclosingElement as TypeElement).toClassName().registryInjectionModuleName()
        val referencesGeneratedModule = includes
            .map { it.toTypeName() }
            .any { it == generatedModuleName }
        if (!referencesGeneratedModule) {
          error("@RegistryModule's @Module must include ${generatedModuleName.simpleName()}",
              userModule)
        }
      }
    }
    return false
  }

  private fun RoundEnvironment.findRegistryCandidateTypeElements(): List<TypeElement> {
    return getElementsAnnotatedWith(registerType).castEach()
  }

  private fun TypeElement.toRegistryElementsOrNull(): RegistryElements? {
    val registerAnnotation = getAnnotation(REGISTER)!!

    val registryModuleTypes = enclosedElements.filter { it.kind == CLASS }
        .castEach<TypeElement>()
        .filter { it.hasAnnotation(REGISTRY_MODULE) }

    if (registryModuleTypes.size > 1) {
      error("Multiple @RegistryModule-annotated types found", this)
    }

    val registryModuleTypeElement =
      if (registryModuleTypes.singleOrNull()?.hasAnnotation("dagger.Module") == false) {
        error("@RegistryModule must also be annotated as a Dagger @Module", this)
        null
      } else {
        registryModuleTypes.singleOrNull()
      }

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

    return RegistryElements(this, registryModuleTypeElement, binders, binderViewHolders, staticContentLayouts)
  }

  private fun RegistryElements.toRegistryClassOrNull(): RegistryClass? {
    val registerAnnotation = targetType.getAnnotation(REGISTER)!!

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
    val injected = registryModuleElement != null
    val bindingSets = builders.values.map { it.build() }
    val indexedViewHolderTypes = viewTypes.map { Pair(it.value, it.key) }
    val staticContentLayouts =
      idScanner.elementToIds(targetType, registerAnnotation, staticContentLayouts).values.toList()
    return RegistryClass(
        registeredAnnotation, public, injected, bindingSets, indexedViewHolderTypes, staticContentLayouts)
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

  private fun RegistryElements.toRegistryModuleElementsOrNull(): RegistryModuleElements? {
    val viewHolderTypes = binderViewHolders +
        binders.map { asTypeElement(it.typeArgumentOf(BINDER, 1)) }

    val injections = viewHolderTypes.groupBy { it.toInjectionType() }
    if (!injections[INVALID].isNullOrEmpty()) {
      return null
    }

    val viewHolderInjections = injections[VIEW_HOLDER_INJECTION].orEmpty()
    val javaxInjections = injections[JAVAX_INJECTION].orEmpty()
    val nonInjections = injections[NONE].orEmpty()
    return RegistryModuleElements(targetType, registryModuleElement!!, viewHolderInjections, javaxInjections, nonInjections)
  }

  private fun RegistryModuleElements.toRegistryInjectionModule(): RegistryInjectionModule {
    val registeredAnnotation = targetType.toClassName()
    val public = PUBLIC in registryModuleElement.modifiers
    val viewHolderInjections = viewHolderInjectionTypes.map { it.toClassName() }
    val javaxInjections = javaxInjectionTypes.map { it.toClassName() }
    return RegistryInjectionModule(registeredAnnotation, public, viewHolderInjections, javaxInjections)
  }

  private fun TypeElement.toInjectionType(): InjectionType {
    val constructors = enclosedElements.filter { it.kind == CONSTRUCTOR }
    val injectionTypes = constructors.map {
      when {
        it.hasAnnotation("com.github.fengdai.inject.viewholder.ViewHolderInject") -> VIEW_HOLDER_INJECTION
        it.hasAnnotation("javax.inject.Inject") -> JAVAX_INJECTION
        else -> NONE
      }
    }
    return injectionTypes.fold(NONE) { acc, type ->
      when (acc) {
        INVALID -> INVALID
        NONE -> if (type == NONE) NONE else type
        JAVAX_INJECTION -> if (type == NONE) JAVAX_INJECTION else INVALID
        VIEW_HOLDER_INJECTION -> if (type == NONE) VIEW_HOLDER_INJECTION else INVALID
      }
    }
  }

  private fun writeRegistryModule(
    elements: RegistryModuleElements,
    module: RegistryInjectionModule
  ) {
    val generatedTypeSpec = module.brewJava()
        .toBuilder()
        .addOriginatingElement(elements.targetType)
        .build()
    JavaFile.builder(module.generatedType.packageName(), generatedTypeSpec)
        .addFileComment("Generated by @RegistryModule. Do not modify!")
        .build()
        .writeTo(filer)
  }

  private fun error(
    message: String,
    element: Element? = null
  ) {
    processingEnv.messager.printMessage(ERROR, message, element)
  }

  private data class RegistryElements(
    val targetType: TypeElement,
    val registryModuleElement: TypeElement?,
    val binders: Set<TypeElement>,
    val binderViewHolders: Set<TypeElement>,
    val staticContentLayouts: Set<Int>
  )

  private data class RegistryModuleElements(
    val targetType: TypeElement,
    val registryModuleElement: TypeElement,
    val viewHolderInjectionTypes: List<TypeElement>,
    val javaxInjectionTypes: List<TypeElement>,
    val nonInjectionTypes: List<TypeElement>
  )

  private enum class InjectionType {
    NONE, JAVAX_INJECTION, VIEW_HOLDER_INJECTION, INVALID
  }
}
