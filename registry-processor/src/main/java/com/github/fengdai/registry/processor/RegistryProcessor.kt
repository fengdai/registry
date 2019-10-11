package com.github.fengdai.registry.processor

import com.github.fengdai.registry.processor.RegistryProcessor.InjectionType.INVALID
import com.github.fengdai.registry.processor.RegistryProcessor.InjectionType.JAVAX_INJECTION
import com.github.fengdai.registry.processor.RegistryProcessor.InjectionType.NONE
import com.github.fengdai.registry.processor.RegistryProcessor.InjectionType.VIEW_HOLDER_INJECTION
import com.github.fengdai.registry.processor.internal.MirrorValue
import com.github.fengdai.registry.processor.internal.MirrorValue.Type
import com.github.fengdai.registry.processor.internal.cast
import com.github.fengdai.registry.processor.internal.castEach
import com.github.fengdai.registry.processor.internal.filterNotNullValues
import com.github.fengdai.registry.processor.internal.getAnnotation
import com.github.fengdai.registry.processor.internal.getValue
import com.github.fengdai.registry.processor.internal.hasAnnotation
import com.github.fengdai.registry.processor.internal.rawClassName
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
import javax.lang.model.element.ElementKind.CLASS
import javax.lang.model.element.ElementKind.CONSTRUCTOR
import javax.lang.model.element.ElementKind.METHOD
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic.Kind.ERROR

private const val REGISTER = "com.github.fengdai.registry.Register"
private const val IDENTIFIER = "com.github.fengdai.registry.Identifier"
private const val REGISTRY_MODULE = "com.github.fengdai.registry.RegistryModule"
private const val BINDER_VIEW_HOLDER = "com.github.fengdai.registry.BinderViewHolder"
private const val BINDER = "com.github.fengdai.registry.Binder"

@AutoService(Processor::class)
@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.ISOLATING)
class RegistryProcessor : AbstractProcessor() {
  override fun getSupportedAnnotationTypes(): Set<String> = setOf(
      REGISTER,
      IDENTIFIER,
      REGISTRY_MODULE
  )

  override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

  override fun init(env: ProcessingEnvironment) {
    super.init(env)
    elements = env.elementUtils
    types = env.typeUtils
    filer = env.filer

    registerType = elements.getTypeElement(REGISTER)
    identifierType = elements.getTypeElement(IDENTIFIER)
    registryModuleType = elements.getTypeElement(REGISTRY_MODULE)
  }

  private lateinit var elements: Elements
  private lateinit var types: Types
  private lateinit var filer: Filer

  private lateinit var registerType: TypeElement
  private lateinit var identifierType: TypeElement
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

    env.getElementsAnnotatedWith(identifierType)
        .filter { !it.enclosingElement.hasAnnotation(REGISTER) }
        .forEach {
          error("@Identifier must be used in a @Register-annotated type.", it)
        }

    env.getElementsAnnotatedWith(registryModuleType)
        .filter { it.enclosingElement.kind != CLASS || !it.enclosingElement.hasAnnotation(REGISTER) }
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
            .filterIsInstance<Type>()

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

    val invalidBinders = binders.filter { binder ->
      var invalid = false
      val defaultConstructor = binder.enclosedElements.filter { it.kind == CONSTRUCTOR }
          .castEach<ExecutableElement>()
          .singleOrNull { it.parameters.isEmpty() }
      if (defaultConstructor == null) {
        error("Binder must have default constructor.", binder)
        invalid = true
      } else if (PUBLIC !in defaultConstructor.modifiers) {
        error("Binder's constructor must be public.", binder)
        invalid = true
      }
      invalid
    }
    if (invalidBinders.isNotEmpty()) {
      return null
    }

    val binderViewHolders =
      (getAnnotationValue(registerAnnotation, "binderViewHolders") as Attribute.Array).value
          .filterIsInstance<Attribute.Class>()
          .map { asTypeElement(it.value) }
          .toSet()

    val identifiers = enclosedElements.filter { it.kind == METHOD }
        .castEach<ExecutableElement>()
        .filter { it.hasAnnotation(IDENTIFIER) }

    val invalidIdentifiers = identifiers.filter { identifier ->
      var invalid = false
      if (PRIVATE in identifier.modifiers) {
        error("@Identifier-annotated methods must not be private.", identifier)
        invalid = true
      }
      if (STATIC !in identifier.modifiers) {
        error("@Identifier-annotated methods must be static.", identifier)
        invalid = true
      }
      if (identifier.parameters.size != 1) {
        error("@Identifier-annotated methods must have one parameter.", identifier)
        invalid = true
      }
      if (identifier.returnType.toTypeName() != TypeName.BOOLEAN) {
        error("@Identifier-annotated methods must return boolean.", identifier)
        invalid = true
      }
      invalid
    }
    if (invalidIdentifiers.isNotEmpty()) {
      return null
    }

    return RegistryElements(this, registryModuleTypeElement, binders, binderViewHolders, identifiers)
  }

  private fun RegistryElements.toRegistryClassOrNull(): RegistryClass? {
    val registerAnnotation = targetType.getAnnotation(REGISTER)!!

    val viewTypes = mutableMapOf<TypeName, Int>()
    val builders = mutableMapOf<ClassName, BindingSet.Builder>()
    val identifiers = identifiers.groupBy(
        { it.parameters.single().asType().toTypeName().rawClassName() },
        { it to (it.getAnnotation(IDENTIFIER)!!.getValue("value", elements) as? Type)?.value?.toTypeName() }
    ).toMutableMap()

    val binderBindings = binders.map { it.binderToBindingOrNull(viewTypes, identifiers) }
    val binderViewHolderBindings = binderViewHolders.map { it.binderViewHolderToBindingOrNull(viewTypes, identifiers) }
    (binderBindings + binderViewHolderBindings).forEach { binding ->
      if (binding == null) {
        return null
      }
      try {
        builders.getOrPut(binding.dataRawType, { BindingSet.Builder(binding.dataRawType) }).addOrThrow(binding)
      } catch (e: DuplicateBindingException) {
        processingEnv.messager.printMessage(ERROR, e.message, targetType, registerAnnotation)
        return null
      }
    }

    val bindingSets = builders.values.map { builder ->
      val bindingSet = builder.build()
      val bindings = bindingSet.bindings
      if (bindings.size > 1) {
        val identifierNames = bindings.mapNotNull { binding -> binding.identifier }
        if (identifierNames.size < bindings.size - 1) {
          error("""
              |${bindingSet.dataRawType} binds to ${bindings.size} ViewHolders. Provide @Identifier(s) for any ${bindings.size - 1} of them. Found ${identifierNames.size}:
              |
              |${bindings.joinToString("\n\n") { binding ->  "${binding.identifier?.let { "[Provided] $it" } ?: "[Missing] ?"}\n -> ${binding.viewHolderType}${if (binding.targetIsBinder) "(${binding.targetType})" else ""}" }}
              |""".trimMargin(), targetType)
          return null
        }
      }
      bindingSet
    }

    val unusedIdentifiers = identifiers.filterNotNullValues()
        .filterNot { it.value.isEmpty() }
        .flatMap { it.value.map { pair -> pair.first } }
    unusedIdentifiers.forEach {
      error("Useless @Identifier:\n$it".trimMargin(), it)
    }

    val targetClassName = targetType.toClassName()
    val public = PUBLIC in targetType.modifiers
    val injected = registryModuleElement != null
    val indexedViewHolderTypes = viewTypes.map { Pair(it.value, it.key) }

    return RegistryClass(targetClassName, public, injected, bindingSets, indexedViewHolderTypes)
  }

  private fun MutableMap<TypeName, Int>.getViewType(viewHolderType: TypeName): Int =
    getOrPut(viewHolderType, { size })

  private fun TypeElement.binderToBindingOrNull(
    viewTypes: MutableMap<TypeName, Int>,
    identifiers: MutableMap<ClassName, List<Pair<ExecutableElement, TypeName?>>>
  ): Binding? {
    val binderType = toClassName()
    val dataType = typeArgumentOf(BINDER, 0)!!.toTypeName().rawClassName()
    val viewHolderType = typeArgumentOf(BINDER, 1)!!.toTypeName()
    val identifier = try {
      identifiers.getAndRemoveIdentifierOrNull(dataType, viewHolderType, binderType)
    } catch (e: DuplicateIdentifierException) {
      return null
    }
    return Binding(binderType, true, dataType, viewHolderType, viewTypes.getViewType(viewHolderType), identifier)
  }

  private fun TypeElement.binderViewHolderToBindingOrNull(
    viewTypes: MutableMap<TypeName, Int>,
    identifiers: MutableMap<ClassName, List<Pair<ExecutableElement, TypeName?>>>
  ): Binding? {
    val dataType = typeArgumentOf(BINDER_VIEW_HOLDER, 0)!!.toTypeName().rawClassName()
    val viewHolderType = toClassName()
    val identifier = try {
      identifiers.getAndRemoveIdentifierOrNull(dataType, viewHolderType, null)
    } catch (e: DuplicateIdentifierException) {
      return null
    }
    return Binding(viewHolderType, false, dataType, viewHolderType, viewTypes.getViewType(viewHolderType), identifier)
  }

  private fun MutableMap<ClassName, List<Pair<ExecutableElement, TypeName?>>>.getAndRemoveIdentifierOrNull(
    dataType: ClassName,
    viewHolderType: TypeName,
    binderType: ClassName?
  ): ExecutableElement? {
    val identifiers = this[dataType].orEmpty().toMutableList()
    val identifierCandidates = identifiers.filter { it.second == viewHolderType }
    return when {
      identifierCandidates.size == 1 -> {
        val result = identifierCandidates.single()
        identifiers.remove(result)
        this[dataType] = identifiers
        result.first
      }
      identifierCandidates.size > 1 -> {
        error("""
            |Multiple @Identifier-annotated methods for
            |$dataType
            |-> $viewHolderType${if (binderType != null) "($binderType)" else ""}:
            |
            |${identifierCandidates.joinToString("\n") { it.first.toString() }}""".trimMargin(),
            identifierCandidates.first().first)
        throw DuplicateIdentifierException()
      }
      else -> null
    }
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
    val identifiers: List<ExecutableElement>
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
