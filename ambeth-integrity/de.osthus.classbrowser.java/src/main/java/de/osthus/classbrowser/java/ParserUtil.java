package de.osthus.classbrowser.java;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * Helper class which supports the parsing of the assemblies and offers some convenience methods.
 * 
 * @author juergen.panser
 */
public class ParserUtil
{
	// ---- INNER CLASSES ------------------------------------------------------

	// ---- CONSTANTS ----------------------------------------------------------

	public static final String TYPE_CLASS = "class";
	public static final String TYPE_DELEGATE = "delegate";
	public static final String TYPE_ENUM = "enum";
	public static final String TYPE_INTERFACE = "interface";
	public static final String TYPE_ANNOTATION = "annotation";

	public static final String MODIFIER_PUBLIC = "public";
	public static final String MODIFIER_PROTECTED = "protected";
	public static final String MODIFIER_PRIVATE = "private";
	public static final String MODIFIER_STATIC = "static";
	public static final String MODIFIER_VIRTUAL = "virtual";
	public static final String MODIFIER_FINAL = "final";
	public static final String MODIFIER_ABSTRACT = "abstract";

	public static final List<String> MODIFIERS_CONSTANT = Arrays.asList(MODIFIER_STATIC, MODIFIER_FINAL);

	public static final String JAVA_ANNOTATION_LOG_INSTANCE = "de.osthus.ambeth.log.LogInstance";
	public static final String JAVA_ANNOTATION_AUTOWIRED = "de.osthus.ambeth.ioc.annotation.Autowired";
	public static final String JAVA_ANNOTATION_PROPERTY = "de.osthus.ambeth.config.Property";

	// ---- VARIABLES ----------------------------------------------------------

	// ---- CONSTRUCTORS -------------------------------------------------------

	private ParserUtil()
	{
		// No instances allowed
	}

	// ---- GETTER/SETTER METHODS ----------------------------------------------

	// ---- METHODS ------------------------------------------------------------

	/**
	 * Check the given type literal. If it isn't one of the constants defined in this class an IllegalArgumentException is thrown.
	 * 
	 * @param typeToCheck
	 *            Type to check
	 */
	public static void checkType(String typeToCheck)
	{
		if (TYPE_CLASS.equals(typeToCheck) || TYPE_DELEGATE.equals(typeToCheck) || TYPE_ENUM.equals(typeToCheck) || TYPE_INTERFACE.equals(typeToCheck)
				|| TYPE_ANNOTATION.equals(typeToCheck))
		{
			return;
		}
		throw new IllegalArgumentException("Type '" + typeToCheck + "'is not a valid one.");
	}

	/**
	 * @param classes
	 *            List of classes
	 * @param moduleMap
	 *            Map with the module name of each class file; key is the full qualified class name in LOWER CASE and value the module name
	 * @param modulesToBeAnalyzed
	 *            List of modules to be analyzed; if null or empty all classes in the jar path are analyzed
	 * @return A list of found types as TypeDescription entities; never null (but may be empty)
	 */
	public static List<TypeDescription> analyzeClasses(List<ClassHolder> classes, Map<String, String> moduleMap, List<String> modulesToBeAnalyzed)
	{
		List<TypeDescription> foundTypes = new ArrayList<TypeDescription>();
		if (classes == null)
		{
			return foundTypes;
		}

		for (ClassHolder classHolder : classes)
		{
			Class<?> classToBeAnalyzed = classHolder.getClazz();
			String source = getSource(classHolder);
			String typeType = getTypeType(classToBeAnalyzed);

			if (typeType != null)
			{
				if (isTypeSkipped(classToBeAnalyzed))
				{
					continue;
				}
				String moduleName = getModule(classToBeAnalyzed, moduleMap);
				if (isModuleSkipped(moduleName, modulesToBeAnalyzed))
				{
					continue;
				}

				Package pkg = classToBeAnalyzed.getPackage();
				String namespace = pkg != null ? pkg.getName() : null;
				String simpleName = classToBeAnalyzed.getSimpleName();
				String fullTypeName = classToBeAnalyzed.getName();
				int genericTypeParams = classToBeAnalyzed.getTypeParameters() != null ? classToBeAnalyzed.getTypeParameters().length : 0;
				Class<?> superclass = classToBeAnalyzed.getSuperclass();
				String superclassName = superclass == null || Object.class.equals(superclass) ? null : superclass.getName();

				TypeDescription typeDescription = new TypeDescription(source, moduleName, namespace, simpleName, fullTypeName, typeType, genericTypeParams);
				typeDescription.setSuperType(superclassName);
				addInterfaces(classToBeAnalyzed, typeDescription);
				addAnnotations(classToBeAnalyzed, typeDescription);
				addMethodDescriptions(classToBeAnalyzed, typeDescription);
				addFieldDescriptions(classToBeAnalyzed, typeDescription);

				foundTypes.add(typeDescription);
			}
		}

		return foundTypes;
	}

	/**
	 * Check if all classes of this module have to be skipped.
	 * 
	 * @param moduleName
	 *            Module to check; if null or empty the class is analyzed to let the comparer give a hint on the missing module name
	 * @param modulesToBeAnalyzed
	 *            List of modules to be analyzed; if null or empty all classes in the jar path are analyzed
	 * @return True if the all classes of this module have to be skipped
	 */
	private static boolean isModuleSkipped(String moduleName, List<String> modulesToBeAnalyzed)
	{
		if (modulesToBeAnalyzed == null || modulesToBeAnalyzed.isEmpty())
		{
			return false;
		}
		boolean doAnalyze = StringUtils.isBlank(moduleName) || modulesToBeAnalyzed.contains(moduleName.trim().toLowerCase());
		return !doAnalyze;
	}

	/**
	 * Check if the given class has to be skipped.
	 * 
	 * @param classToBeAnalyzed
	 *            Class to check
	 * @return True if class should be skipped
	 */
	private static boolean isTypeSkipped(Class<?> classToBeAnalyzed)
	{
		boolean skip = false;
		if (classToBeAnalyzed.isAnonymousClass() || classToBeAnalyzed.isLocalClass() || classToBeAnalyzed.isMemberClass()
				|| StringUtils.containsIgnoreCase(classToBeAnalyzed.getName(), "ambeth.repackaged."))
		{
			skip = true;
		}
		return skip;
	}

	/**
	 * Get the module where the type is coming from.
	 * 
	 * @param classToBeAnalyzed
	 *            Class
	 * @param moduleMap
	 *            Map with the module name of each class file; key is the full qualified class name in LOWER CASE and value the module name
	 * @return Module name or empty string (if no module can be found); never null
	 */
	public static String getModule(Class<?> classToBeAnalyzed, Map<String, String> moduleMap)
	{
		if (classToBeAnalyzed == null || moduleMap == null)
		{
			throw new IllegalArgumentException("Mandatory value missing!");
		}
		String classFileName = classToBeAnalyzed.getName().toLowerCase() + ".java";
		String moduleName = moduleMap.get(classFileName);
		if (!StringUtils.isBlank(moduleName))
		{
			return moduleName;
		}
		return StringUtils.EMPTY;
	}

	/**
	 * Get the source where the types are coming from.
	 * 
	 * @param classHolder
	 *            ClassHolder
	 * @return Source
	 */
	public static String getSource(ClassHolder classHolder)
	{
		return classHolder.getSource();
	}

	/**
	 * Get the type (text constant) for the given type. Returns null if the type hasn't to be handled.
	 * 
	 * @param givenType
	 *            Type to identify; mandatory
	 * @return One of the TYPE constants (e.g. TYPE_CLASS) or null
	 */
	public static String getTypeType(Class<?> givenType)
	{
		if (givenType == null)
		{
			throw new IllegalArgumentException("Class may not be null!");
		}

		if (givenType.isAnnotation())
		{
			return TYPE_ANNOTATION;
		}
		else if (givenType.isInterface())
		{
			return TYPE_INTERFACE;
		}
		else if (givenType.isEnum())
		{
			return TYPE_ENUM;
		}
		else
		{
			return TYPE_CLASS;
		}
	}

	protected static void addInterfaces(Class<?> classToBeAnalyzed, TypeDescription typeDescription)
	{
		Class<?>[] interfaces = classToBeAnalyzed.getInterfaces();
		List<String> interfaceNames = typeDescription.getInterfaces();
		for (Class<?> iface : interfaces)
		{
			interfaceNames.add(iface.getName());
		}
	}

	/**
	 * Add all runtime visible annotations from the given type to the given description.
	 * 
	 * @param classToBeAnalyzed
	 *            Type to get the method infos from; mandatory
	 * @param typeDescription
	 *            Description to write the method infos to; mandatory
	 */
	protected static void addAnnotations(Class<?> classToBeAnalyzed, TypeDescription typeDescription)
	{
		List<AnnotationInfo> annotationInfo = getAnnotationInfo(classToBeAnalyzed);
		typeDescription.getAnnotations().addAll(annotationInfo);
	}

	/**
	 * Add all methods from the given type to the given description.
	 * 
	 * @param givenType
	 *            Type to get the method infos from; mandatory
	 * @param typeDescription
	 *            Description to write the method infos to; mandatory
	 */
	protected static void addMethodDescriptions(Class<?> givenType, TypeDescription typeDescription)
	{
		if (givenType == null || typeDescription == null)
		{
			throw new IllegalArgumentException("Mandatory values for adding the method descriptions are missing!");
		}

		for (Method methodInfo : givenType.getDeclaredMethods())
		{
			MethodDescription methodDescription = createMethodDescriptionFrom(methodInfo);
			typeDescription.getMethodDescriptions().add(methodDescription);
		}
	}

	/**
	 * Add all fields from the given type to the given description.
	 * 
	 * @param givenType
	 *            Type to get the field infos from; mandatory
	 * @param typeDescription
	 *            Description to write the method infos to; mandatory
	 */
	protected static void addFieldDescriptions(Class<?> givenType, TypeDescription typeDescription)
	{
		if (givenType == null || typeDescription == null)
		{
			throw new IllegalArgumentException("Mandatory values for adding the field descriptions are missing!");
		}

		List<FieldDescription> fieldDescriptions = typeDescription.getFieldDescriptions();
		for (Field fieldInfo : givenType.getDeclaredFields())
		{
			List<AnnotationInfo> annotationInfo = getAnnotationInfo(fieldInfo);
			FieldDescription fieldDescription = createFieldDescriptionFrom(fieldInfo);
			fieldDescriptions.add(fieldDescription);

			fieldDescription.setEnumConstant(fieldInfo.isEnumConstant());
			fieldDescription.getAnnotations().addAll(annotationInfo);

			// Record the value of primitive and string constants.
			if (fieldDescription.getModifiers().containsAll(MODIFIERS_CONSTANT)
					&& (fieldInfo.getType().isPrimitive() || String.class.equals(fieldInfo.getType())))
			{
				try
				{
					fieldInfo.setAccessible(true);
					Object initialValue = fieldInfo.get(null);
					fieldDescription.setInitialValue(initialValue.toString());
				}
				catch (IllegalArgumentException | IllegalAccessException e)
				{
					throw new RuntimeException(e);
				}
			}
		}
	}

	/**
	 * Create a method description from the given information.
	 * 
	 * @param methodInfo
	 *            Method to get the infos from; mandatory
	 * @return MethodDescription
	 */
	public static MethodDescription createMethodDescriptionFrom(Method methodInfo)
	{
		if (methodInfo == null)
		{
			throw new IllegalArgumentException("Mandatory values for creating a method description are missing!");
		}

		String returnType = getReturnTypeFrom(methodInfo);
		List<String> modifiers = getModifiersFrom(methodInfo);
		List<String> parameterTypes = getParameterTypesFrom(methodInfo);

		MethodDescription methodDescription = new MethodDescription(methodInfo.getName(), returnType, modifiers, parameterTypes);
		List<AnnotationInfo> annotationInfos = getAnnotationInfo(methodInfo);
		methodDescription.getAnnotations().addAll(annotationInfos);

		return methodDescription;
	}

	/**
	 * Create a field description from the given information.
	 * 
	 * @param fieldInfo
	 *            FieldInfo to get the infos from; mandatory
	 * @return FieldDescription
	 */
	public static FieldDescription createFieldDescriptionFrom(Field fieldInfo)
	{
		if (fieldInfo == null)
		{
			throw new IllegalArgumentException("Mandatory values for creating a field description are missing!");
		}

		final String fieldType;
		Type genericType = fieldInfo.getGenericType();
		if (genericType instanceof ParameterizedType)
		{
			fieldType = getTypeFrom(genericType);
		}
		else
		{
			fieldType = getTypeFrom(fieldInfo.getType());
		}
		List<String> modifiers = getModifiersFrom(fieldInfo);

		return new FieldDescription(fieldInfo.getName(), fieldType, modifiers);
	}

	/**
	 * Get the text representation of the methods return type.
	 * 
	 * @param methodInfo
	 *            Method to get the infos from; mandatory
	 * @return Return type as text
	 */
	public static String getReturnTypeFrom(Method methodInfo)
	{
		if (methodInfo == null)
		{
			throw new IllegalArgumentException("Mandatory values to get the return type are missing!");
		}

		Type returnType = methodInfo.getGenericReturnType();
		if (returnType instanceof ParameterizedType)
		{
			return getTypeFrom(returnType);
		}

		return getTypeFrom(methodInfo.getReturnType());
	}

	/**
	 * Get the text representation of the given type.
	 * 
	 * @param givenType
	 *            Type to get the infos from; mandatory
	 * @return Type as text
	 */
	public static String getTypeFrom(Type givenType)
	{
		if (givenType == null)
		{
			throw new IllegalArgumentException("Mandatory values to get the type are missing!");
		}
		return givenType.toString();
	}

	/**
	 * Get the text representation of the given type.
	 * 
	 * @param givenType
	 *            Type to get the infos from; mandatory
	 * @return Type as text
	 */
	public static String getTypeFrom(Class<?> givenType)
	{
		if (givenType == null)
		{
			throw new IllegalArgumentException("Mandatory values to get the type are missing!");
		}

		String result = givenType.getCanonicalName();
		return result;
	}

	/**
	 * Get the text representation of all method modifiers.
	 * 
	 * @param methodInfo
	 *            Method to get the infos from; mandatory
	 * @return List of modifiers; never null (but may be empty)
	 */
	public static List<String> getModifiersFrom(Method methodInfo)
	{
		if (methodInfo == null)
		{
			throw new IllegalArgumentException("Mandatory values to get the method modifiers are missing!");
		}

		int modifierFlags = methodInfo.getModifiers();
		return getModifersFrom(modifierFlags);
	}

	/**
	 * Get the text representation of all field modifiers.
	 * 
	 * @param fieldInfo
	 *            Field to get the infos from; mandatory
	 * @return List of modifiers; never null (but may be empty)
	 */
	public static List<String> getModifiersFrom(Field fieldInfo)
	{
		if (fieldInfo == null)
		{
			throw new IllegalArgumentException("Mandatory values to get the field modifiers are missing!");
		}

		int modifierFlags = fieldInfo.getModifiers();
		return getModifersFrom(modifierFlags);
	}

	/**
	 * @param modifierFlags
	 *            Modifier flags (integer constants)
	 * @return List of modifiers; never null (but may be empty)
	 */
	protected static List<String> getModifersFrom(int modifierFlags)
	{
		List<String> modifiers = new ArrayList<String>();
		if (Modifier.isPublic(modifierFlags))
		{
			modifiers.add(MODIFIER_PUBLIC);
		}
		if (Modifier.isProtected(modifierFlags))
		{
			modifiers.add(MODIFIER_PROTECTED);
		}
		if (Modifier.isPrivate(modifierFlags))
		{
			modifiers.add(MODIFIER_PRIVATE);
		}
		if (Modifier.isStatic(modifierFlags))
		{
			modifiers.add(MODIFIER_STATIC);
		}
		if (Modifier.isFinal(modifierFlags))
		{
			modifiers.add(MODIFIER_FINAL);
		}
		if (Modifier.isAbstract(modifierFlags))
		{
			modifiers.add(MODIFIER_ABSTRACT);
		}
		return modifiers;
	}

	/**
	 * Get the text representation of all method parameter types.
	 * 
	 * @param methodInfo
	 *            Method to get the infos from; mandatory
	 * @return List of parameter types; never null (but may be empty)
	 */
	public static List<String> getParameterTypesFrom(Method methodInfo)
	{
		if (methodInfo == null)
		{
			throw new IllegalArgumentException("Mandatory values to get the parameter types are missing!");
		}

		int parameterCount = methodInfo.getGenericParameterTypes().length;
		String[] parameters = new String[parameterCount];
		for (int i = 0; i < parameterCount; i++)
		{
			Type parameterType = methodInfo.getGenericParameterTypes()[i];
			if (parameterType instanceof ParameterizedType)
			{
				parameters[i] = getTypeFrom(parameterType);
			}
		}
		for (int i = 0; i < parameterCount; i++)
		{
			if (StringUtils.isBlank(parameters[i]))
			{
				Class<?> parameterType = methodInfo.getParameterTypes()[i];
				parameters[i] = getTypeFrom(parameterType);
			}
		}

		return Arrays.asList(parameters);
	}

	/**
	 * Extracts the full info of all annotations present on the annotated element.
	 * 
	 * @param annotatedElement
	 *            Object to be analyzed
	 * @return List of the full annotation info
	 */
	protected static ArrayList<AnnotationInfo> getAnnotationInfo(AnnotatedElement annotatedElement)
	{
		ArrayList<AnnotationInfo> annotationInfoList = new ArrayList<>();
		Annotation[] annotations = annotatedElement.getAnnotations();
		for (Annotation annotation : annotations)
		{
			Class<? extends Annotation> annotationType = annotation.annotationType();
			String annotationTypeName = annotationType.getName();

			Method[] methods = annotationType.getMethods();
			ArrayList<AnnotationParamInfo> params = new ArrayList<>();
			for (Method method : methods)
			{
				Class<?> declaringClass = method.getDeclaringClass();
				String declaringClassName = declaringClass.getName();
				if (!annotationTypeName.equals(declaringClassName))
				{
					continue;
				}

				String paramName = method.getName();
				String paramType = method.getReturnType().getName();
				Object defaultValue = method.getDefaultValue();
				Object currentValue = null;
				try
				{
					currentValue = method.invoke(annotation);
				}
				catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
				{
					throw new RuntimeException(e);
				}
				AnnotationParamInfo param = new AnnotationParamInfo(paramName, paramType, defaultValue, currentValue);
				params.add(param);
			}

			AnnotationInfo info = new AnnotationInfo(annotationTypeName, params);
			annotationInfoList.add(info);
		}
		return annotationInfoList;
	}

	public static boolean containsAnnotation(List<AnnotationInfo> annotationInfo, String annotationType)
	{
		for (AnnotationInfo annotation : annotationInfo)
		{
			String type = annotation.getAnnotationType();
			if (type.equals(annotationType))
			{
				return true;
			}
		}
		return false;
	}
}
