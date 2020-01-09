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
import com.google.auto.common.AnnotationMirrors
import com.google.auto.common.MoreElements
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
import javax.lang.model.element.ElementKind.INTERFACE
import javax.lang.model.element.ElementKind.METHOD
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic.Kind.ERROR

private const val ITEM = "com.github.fengdai.registry.RegistryItem"
private const val REGISTRY = "com.github.fengdai.registry.Registry"
private const val REGISTRY_ITEM = "com.github.fengdai.registry.Registry.Item"
private const val REGISTRY_MODULE = "com.github.fengdai.registry.Registry.Module"
private const val BINDS_BINDER = "com.github.fengdai.registry.BindsBinder"
private const val BINDS_VIEW_HOLDER = "com.github.fengdai.registry.BindsViewHolder"
private const val BINDS_LAYOUT = "com.github.fengdai.registry.BindsLayout"
private const val BINDER_VIEW_HOLDER = "com.github.fengdai.registry.BinderViewHolder"
private const val BINDER = "com.github.fengdai.registry.Binder"

private val BINDS_ANNOTATIONS = setOf(BINDS_BINDER, BINDS_VIEW_HOLDER, BINDS_LAYOUT)

@AutoService(Processor::class)
@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.ISOLATING)
class RegistryProcessor : AbstractProcessor() {
  override fun getSupportedAnnotationTypes(): Set<String> =
    setOf(
        REGISTRY,
        REGISTRY_ITEM,
        REGISTRY_MODULE
    ) + BINDS_ANNOTATIONS

  override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

  override fun init(env: ProcessingEnvironment) {
    super.init(env)
    elements = env.elementUtils
    types = env.typeUtils
    filer = env.filer
    idScanner = IdScanner(processingEnv)

    itemInterface = elements.getTypeElement(ITEM)
    itemInterfaceMethods = itemInterface.enclosedElements.filter { it.kind == METHOD }.castEach()
    registryType = elements.getTypeElement(REGISTRY)
    registryItemType = elements.getTypeElement(REGISTRY_ITEM)
    registryModuleType = elements.getTypeElement(REGISTRY_MODULE)
  }

  private lateinit var elements: Elements
  private lateinit var types: Types
  private lateinit var filer: Filer
  private lateinit var idScanner: IdScanner

  private lateinit var itemInterface: TypeElement
  private lateinit var itemInterfaceMethods: List<ExecutableElement>
  private lateinit var registryType: TypeElement
  private lateinit var registryItemType: TypeElement
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
        .filter { it.registryModuleType != null }
        .mapNotNull { it.toRegistryModuleElementsOrNull() }

    registryModuleElements.associateWith { it.toRegistryInjectionModule() }
        .forEach(::writeRegistryModule)

    env.getElementsAnnotatedWith(registryItemType)
        .filter { !it.enclosingElement.hasAnnotation(REGISTRY) }
        .forEach { registryItem ->
          error("@Registry.Item must be declared as a nested type of a @Registry-annotated type.",
              registryItem)
        }

    env.getElementsAnnotatedWith(registryModuleType)
        .filter { !it.enclosingElement.hasAnnotation(REGISTRY) }
        .forEach { registryModule ->
          error("@Registry.Module must be declared as a nested type of a @Registry-annotated type.",
              registryModule)
        }

    userModules += registryModuleElements.map { it.registryModuleElement.qualifiedName.toString() }

    // Wait until processing is ending to validate that the @Registry.Module's @Module annotation
    // includes the generated type.
    if (userModules.isNotEmpty() && env.processingOver()) {
      userModules.forEach { userModuleFqcn ->
        // In the processing round in which we handle the @Registry.Module the @Module annotation's
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
          error("@Registry.Module's @Module must include ${generatedModuleName.simpleName()}",
              userModule)
        }
      }
    }
    return false
  }

  private fun RoundEnvironment.findRegistryCandidateTypeElements(): List<TypeElement> {
    return getElementsAnnotatedWith(registryType).castEach()
  }

  private fun TypeElement.toRegistryElementsOrNull(): RegistryElements? {
    var valid = true

    if (kind != INTERFACE) {
      error("@Registry must be an interface.", this)
      valid = false
    } else if (PRIVATE in modifiers) {
      error("@Registry interfaces must not be private.", this)
      valid = false
    } else if (interfaces.isNotEmpty()) {
      error("@Registry interfaces must not extend other interfaces.", this)
      valid = false
    }

    if (!valid) return null

    val registryItemTypes = enclosedElements
        .filter { it.hasAnnotation(REGISTRY_ITEM) }
        .castEach<TypeElement>()
    if (registryItemTypes.isEmpty()) {
      error("No nested @Registry.Item found.", this)
      valid = false
    } else if (registryItemTypes.size > 1) {
      error("Multiple @Registry.Item types found.", this)
      valid = false
    }

    if (!valid) return null

    val registryItemType = registryItemTypes.single()
    val registryItemTypeMirror = registryItemType.asType()

    if (registryItemType.kind != INTERFACE) {
      error("@Registry.Item must be an interface.", registryItemType)
      valid = false
    } else if (registryItemType.interfaces.none { it == itemInterface.asType() }) {
      error("@Registry.Item must extend RegistryItem.", registryItemType)
      valid = false
    } else if (registryItemType.interfaces.size > 1) {
      error("@Registry.Item can only extend RegistryItem.", registryItemType)
      valid = false
    }

    registryItemType.enclosedElements.filter { it.kind == METHOD }
        .castEach<ExecutableElement>()
        .filterNot { it.isDefault }
        .filter { it !in itemInterfaceMethods }
        .forEach {
          error("@Registry.Item can't have abstract methods.", it)
          valid = false
        }

    if (!valid) return null

    val itemFactoryMethods =
      MoreElements.getLocalAndInheritedMethods(this, types, elements)
          .filterNot { it.isDefault } // Ignore default methods for convenience overloads.
          .groupBy { method ->
            if (method.returnType != registryItemTypeMirror) {
              error("@Registry's factory method must return $registryItemType", method)
              valid = false
            }

            val bindsAnnotations = method.annotationMirrors
                .map { it.annotationType.toString() }
                .filter { it in BINDS_ANNOTATIONS }
            if (bindsAnnotations.isEmpty()) {
              error("@Registry's factory method requires an annotation such as " +
                  "@BindsBinder, @BindsViewHolder or @BindsLayout", method)
              valid = false
            } else if (bindsAnnotations.size > 1) {
              error("@Registry's factory method can't have multiple annotations of " +
                  "@BindsBinder, @BindsViewHolder or @BindsLayout", method)
              valid = false
            }
            val bindsAnnotation = bindsAnnotations.singleOrNull()
            when (bindsAnnotation) {
              BINDS_BINDER -> {
                if (method.parameters.isEmpty()) {
                  error("@BindsBinder must have a parameter of data type", method)
                  valid = false
                } else if (method.parameters.size > 1) {
                  error("@BindsBinder can't have multiple parameters", method)
                  valid = false
                }
              }
              BINDS_VIEW_HOLDER -> {
                if (method.parameters.isEmpty()) {
                  error("@BindsViewHolder must have a parameter of data type", method)
                  valid = false
                } else if (method.parameters.size > 1) {
                  error("@BindsViewHolder can't have multiple parameters", method)
                  valid = false
                }
              }
              BINDS_LAYOUT -> {
                if (method.parameters.isNotEmpty()) {
                  error("@BindsLayout can't have parameters", method)
                  valid = false
                }
              }
            }
            bindsAnnotation
          }

    if (!valid) return null

    val registryModuleTypes = enclosedElements.filter { it.kind == CLASS }
        .castEach<TypeElement>()
        .filter { it.hasAnnotation(REGISTRY_MODULE) }
    if (registryModuleTypes.size > 1) {
      error("Multiple @Registry.Module types found", this)
      valid = false
    }
    val registryModuleType = registryModuleTypes.singleOrNull()
    if (registryModuleType?.hasAnnotation("dagger.Module") == false) {
      error("@Registry.Module must also be annotated as a Dagger @Module", this)
      valid = false
    }

    if (!valid) return null

    val binders = itemFactoryMethods[BINDS_BINDER].orEmpty()
    val binderViewHolders = itemFactoryMethods[BINDS_VIEW_HOLDER].orEmpty()
    val layouts = itemFactoryMethods[BINDS_LAYOUT].orEmpty()
    return RegistryElements(this, registryItemType, registryModuleType, binders, binderViewHolders, layouts)
  }

  private fun RegistryElements.toRegistryClassOrNull(): RegistryClass? {
    val viewTypes = mutableMapOf<TypeName, Int>()
    val builders = mutableMapOf<ClassName, BindingSet.Builder>()

    var valid = true

    val binderBindings = binders.map { it.binderToBindingOrNull(viewTypes) }
    val binderViewHolderBindings = binderViewHolders.map { it.binderViewHolderToBindingOrNull(viewTypes) }
    (binderBindings + binderViewHolderBindings).forEach { binding ->
      if (binding == null) {
        valid = false
      } else {
        try {
          builders.getOrPut(binding.dataRawType, { BindingSet.Builder(binding.dataRawType) }).add(binding)
        } catch (e: DuplicateBindingException) {
          error(e.message!!, targetType)
          valid = false
        }
      }
    }

    val layoutBindings = layouts.map { LayoutBinding(it, it.layout) }
    layoutBindings.groupBy { it.layout }
        .forEach { (id, layoutBindings) ->
          if (layoutBindings.size > 1) {
            error("Duplicate @BindsLayouts for ${id.code}:\n* "
                + layoutBindings.joinToString("\n* ") { it.factoryMethod.toString() },
                layoutBindings.first().factoryMethod)
            valid = false
          }
        }

    if (!valid) return null

    val targetClassName = targetType.toClassName()
    val registryItemClassName = registryItemType.toClassName()
    val public = PUBLIC in targetType.modifiers
    val injected = registryModuleType != null
    val bindingSets = builders.values.map { it.build() }
    val indexedViewHolderTypes = viewTypes.map { Pair(it.value, it.key) }
    return RegistryClass(targetClassName, registryItemClassName, public, injected, bindingSets, indexedViewHolderTypes, layoutBindings)
  }

  private fun MutableMap<TypeName, Int>.getViewType(viewHolderType: TypeName): Int =
    getOrPut(viewHolderType, { size })

  private val ExecutableElement.binderType: TypeElement?
    get() = ((getAnnotation(BINDS_BINDER)!!.getValue("value", elements) as? MirrorValue.Type)?.value as? DeclaredType)?.asElement() as? TypeElement

  private val ExecutableElement.binderViewHolderType : TypeElement?
    get() = ((getAnnotation(BINDS_VIEW_HOLDER)!!.getValue("value", elements) as? MirrorValue.Type)?.value as? DeclaredType)?.asElement() as? TypeElement

  private val ExecutableElement.layout: Id
    get() {
      val annotation = getAnnotation(BINDS_LAYOUT)!!
      val layoutValue = (AnnotationMirrors.getAnnotationValue(annotation, "value") as Attribute.Constant).value as Int
      return idScanner.elementToId(this, annotation, layoutValue)
    }

  private fun ExecutableElement.binderToBindingOrNull(viewTypes: MutableMap<TypeName, Int>): Binding? {
    val binderElement = binderType ?: return null
    val constructors = binderElement.enclosedElements.filter { it.kind == CONSTRUCTOR }
        .castEach<ExecutableElement>()
    if (constructors.isEmpty()) {
      return null
    } else if (constructors.none { it.parameters.isEmpty() }) {
      error("Binder must have a no-argument constructor.", binderElement)
      return null
    }
    val constructor = constructors.single { it.parameters.isEmpty() }
    if (PUBLIC !in constructor.modifiers) {
      error("Binder's constructor must be public.", constructor)
      return null
    }

    val dataType = binderElement.typeArgumentOf(BINDER, 0)!!
    val parameterDataType =  parameters.single().asType()
    if (!types.isAssignable(dataType, parameterDataType)) {
      error("@BindsBinder methods' parameter type must be assignable to the Binder's data type.\n"
          + "[$parameterDataType] is not assignable to \n[$dataType]", this)
      return null
    }
    val dataTypeName = dataType.toTypeName()
    val binderClassName = binderElement.toClassName()
    val viewHolderTypeName = binderElement.typeArgumentOf(BINDER, 1)!!.toTypeName()
    return Binding(this, binderClassName, true, dataTypeName, viewHolderTypeName, viewTypes.getViewType(viewHolderTypeName))
  }

  private fun ExecutableElement.binderViewHolderToBindingOrNull(viewTypes: MutableMap<TypeName, Int>): Binding? {
    val binderViewHolderElement = binderViewHolderType ?: return null
    val dataType = binderViewHolderElement.typeArgumentOf(BINDER_VIEW_HOLDER, 0)!!
    val parameterDataType =  parameters.single().asType()
    if (!types.isAssignable(dataType, parameterDataType)) {
      error("@BindsViewHolder methods' parameter type must be assignable to the BinderViewHolder's data type.\n"
          + "[$parameterDataType] is not assignable to \n[$dataType]", this)
      return null
    }
    val dataTypeName = dataType.toTypeName()
    val viewHolderTypeName = binderViewHolderElement.toClassName()
    return Binding(this, viewHolderTypeName, false, dataTypeName, viewHolderTypeName, viewTypes.getViewType(viewHolderTypeName))
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
        .addFileComment("Generated by @Registry. Do not modify!")
        .build()
        .writeTo(filer)
  }

  private fun RegistryElements.toRegistryModuleElementsOrNull(): RegistryModuleElements? {
    val viewHolderTypes = (binderViewHolders.mapNotNull { it.binderViewHolderType } +
        binders.mapNotNull { it.binderType }.map { asTypeElement(it.typeArgumentOf(BINDER, 1)) }).toSet()

    val injections = viewHolderTypes.groupBy { it.toInjectionType() }
    if (!injections[INVALID].isNullOrEmpty()) {
      return null
    }

    val viewHolderInjections = injections[VIEW_HOLDER_INJECTION].orEmpty()
    val javaxInjections = injections[JAVAX_INJECTION].orEmpty()
    val nonInjections = injections[NONE].orEmpty()
    return RegistryModuleElements(targetType, registryModuleType!!, viewHolderInjections, javaxInjections, nonInjections)
  }

  private fun RegistryModuleElements.toRegistryInjectionModule(): RegistryInjectionModule {
    val targetClassName = targetType.toClassName()
    val public = PUBLIC in registryModuleElement.modifiers
    val viewHolderInjections = viewHolderInjectionTypes.map { it.toClassName() }
    val javaxInjections = javaxInjectionTypes.map { it.toClassName() }
    return RegistryInjectionModule(targetClassName, public, viewHolderInjections, javaxInjections)
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
        .addFileComment("Generated by @Registry.Module. Do not modify!")
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
    val registryItemType: TypeElement,
    val registryModuleType: TypeElement?,
    val binders: List<ExecutableElement>,
    val binderViewHolders: List<ExecutableElement>,
    val layouts: List<ExecutableElement>
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
