package com.koch.classbrowser.compare;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.koch.classbrowser.java.AnnotationInfo;
import com.koch.classbrowser.java.FieldDescription;
import com.koch.classbrowser.java.INamed;
import com.koch.classbrowser.java.MethodDescription;
import com.koch.classbrowser.java.ParserUtil;
import com.koch.classbrowser.java.TypeDescription;

/**
 * Helper class for the comparing.
 *
 * @author juergen.panser
 */
public class CompareUtil {

	private static final String KNOWN_CLASSES_FILE = "knownJavaClassesToCSharpTypes.properties";

	public static final String TYPE_VOID = "void";

	public static final String LOG_PROPERTY_GETTER_NAME = "get_Log";
	public static final String LOG_PROPERTY_SETTER_NAME = "set_Log";
	public static final String LOG_PROPERTY_FIELD_NAME = "log";
	public static final String LOG_TYPE_CSHARP = "De.Osthus.Ambeth.Log.ILogger";
	public static final String LOG_TYPE_JAVA = "com.koch.ambeth.log.ILogger";

	private static final Pattern CSHARP_GETTER_NAME = Pattern.compile("get_([A-Z][a-zA-Z0-9]*)");
	private static final Pattern CSHARP_SETTER_NAME = Pattern.compile("set_([A-Z][a-zA-Z0-9]*)");

	private static final HashSet<String> NOT_INJECTABLE_JAVA_TYPES =
			new HashSet<>(Arrays.asList("boolean", "int", "long", "java.lang.Class", "java.lang.String"));

	private static final HashSet<String> JAVA_CLASSES_WITH_EXTERNAL_CSHARP_TYPES = new HashSet<>();
	private static final HashSet<String> CSHARP_TYPES_WITH_EXTERNAL_JAVA_CLASSES = new HashSet<>();

	private static final HashMap<String, HashSet<String>> JAVA_CLASS_TO_CSHARP_TYPES =
			new HashMap<>();

	private static final HashMap<String, String> CSHARP_NAMESPACE_TO_JAVA_PACKAGE = new HashMap<>();

	static {
		try {
			ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
			InputStream inputStream = contextClassLoader.getResourceAsStream(KNOWN_CLASSES_FILE);
			Properties knownClassesProperties = new Properties();
			knownClassesProperties.load(inputStream);

			HashMap<String, HashSet<String>> javaClasseToCsharpTypes = JAVA_CLASS_TO_CSHARP_TYPES;

			Pattern oneWhitespace = Pattern.compile("\\s");
			Iterator<Entry<Object, Object>> iter = knownClassesProperties.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<Object, Object> next = iter.next();
				String key = (String) next.getKey();
				String value = (String) next.getValue();

				HashSet<String> knownCSharpTypes = new HashSet<>();
				javaClasseToCsharpTypes.put(key, knownCSharpTypes);

				String[] values = oneWhitespace.split(value);
				for (String cSharpType : values) {
					knownCSharpTypes.add(cSharpType);
				}
			}

			CSHARP_TYPES_WITH_EXTERNAL_JAVA_CLASSES
					.add("De.Osthus.Ambeth.Annotation.IgnoreAttribute".toLowerCase()); // jUnit
			CSHARP_TYPES_WITH_EXTERNAL_JAVA_CLASSES
					.add("De.Osthus.Ambeth.Annotation.PostLoadAttribute".toLowerCase()); // javax.persistence
			CSHARP_TYPES_WITH_EXTERNAL_JAVA_CLASSES
					.add("De.Osthus.Ambeth.Annotation.PrePersistAttribute".toLowerCase()); // javax.persistence
			CSHARP_TYPES_WITH_EXTERNAL_JAVA_CLASSES
					.add("De.Osthus.Ambeth.Collections.Specialized.IPropertyChangeListener".toLowerCase()); // java.beans
			CSHARP_TYPES_WITH_EXTERNAL_JAVA_CLASSES
					.add("De.Osthus.Ambeth.Threading.CountDownLatch".toLowerCase()); // java.util.concurrent
			CSHARP_TYPES_WITH_EXTERNAL_JAVA_CLASSES
					.add("De.Osthus.Ambeth.Threading.CyclicBarrier".toLowerCase()); // java.util.concurrent

			JAVA_CLASSES_WITH_EXTERNAL_CSHARP_TYPES
					.add("com.koch.ambeth.util.threading.ISendOrPostCallback".toLowerCase()); // System.Threading

			// Repackaged Java classes
			CSHARP_NAMESPACE_TO_JAVA_PACKAGE.put("De.Osthus.Ambeth.Bytecode.Visitor".toLowerCase(),
					"com.koch.ambeth.repackaged.org.objectweb.asm");
			// Java-Ambeth standard is not a valid C# namespace
			CSHARP_NAMESPACE_TO_JAVA_PACKAGE.put("De.Osthus.Ambeth.Exceptions".toLowerCase(),
					"com.koch.ambeth.exception");
			CSHARP_NAMESPACE_TO_JAVA_PACKAGE.put("De.Osthus.Ambeth.Ioc.Exceptions".toLowerCase(),
					"com.koch.ambeth.ioc.exception");
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// ---- CONSTRUCTORS -------------------------------------------------------

	private CompareUtil() {
		// No instance allowed
	}

	// ---- METHODS ------------------------------------------------------------

	/**
	 * Compare the given types.
	 *
	 * @param javaTypes Map with the full name of the JAVA type as key and the TypeDescription entity
	 *        as value; may be null
	 * @param csharpTypes Map with the full name of the C# type as key and the TypeDescription entity
	 *        as value; may be null
	 * @return List with compare results; never null
	 */
	public static List<CompareResult> compare(SortedMap<String, TypeDescription> javaTypes,
			SortedMap<String, TypeDescription> csharpTypes) {
		if (javaTypes == null || javaTypes.isEmpty() || csharpTypes == null || csharpTypes.isEmpty()) {
			return new ArrayList<>(0);
		}
		List<CompareResult> results = new ArrayList<>(javaTypes.size());
		SortedMap<String, TypeDescription> remainingJavaTypes = new TreeMap<>(javaTypes);
		// First use all the C# classes (because here we additionally have the
		// delegate type)
		for (Entry<String, TypeDescription> csharpTypeEntry : csharpTypes.entrySet()) {
			String fullTypeName = csharpTypeEntry.getKey();
			if (CSHARP_TYPES_WITH_EXTERNAL_JAVA_CLASSES.contains(fullTypeName)) {
				continue;
			}

			String javaFullTypeName = fullTypeName;
			TypeDescription csharpType = csharpTypeEntry.getValue();

			CompareResult result = new CompareResult(fullTypeName);
			result.setCsharpType(csharpType);

			// Try to get a matching Java type
			TypeDescription javaType = findJavaType(javaFullTypeName, csharpType, javaTypes);
			if (javaType == null) {
				int lastIndexOfDot = fullTypeName.lastIndexOf('.');
				String namespace = fullTypeName.substring(0, lastIndexOfDot);
				String packageName = CSHARP_NAMESPACE_TO_JAVA_PACKAGE.get(namespace);
				if (packageName != null) {
					String typeName = fullTypeName.substring(lastIndexOfDot + 1, fullTypeName.length());
					javaFullTypeName = packageName + "." + typeName;
					javaType = findJavaType(javaFullTypeName, csharpType, javaTypes);
				}
			}
			if (javaType != null) {
				remainingJavaTypes.remove(javaType.getFullTypeName().toLowerCase());
				result.setJavaType(javaType);
				compare(result, javaType, csharpType);
			}
			else {
				result.addError(CompareStatus.NO_MATCHING_JAVA_CLASS_FOUND, null);
			}

			results.add(result);
		}
		// The remaining Java classes weren't matched -> there is no C# class
		// for them
		for (Entry<String, TypeDescription> javaTypeEntry : remainingJavaTypes.entrySet()) {
			String fullTypeName = javaTypeEntry.getKey();
			if (JAVA_CLASSES_WITH_EXTERNAL_CSHARP_TYPES.contains(fullTypeName)) {
				continue;
			}

			TypeDescription javaType = javaTypeEntry.getValue();

			CompareResult result = new CompareResult(fullTypeName);
			result.addError(CompareStatus.NO_MATCHING_CSHARP_CLASS_FOUND, null);
			result.setJavaType(javaType);

			results.add(result);
		}
		return results;
	}

	private static TypeDescription findJavaType(String javaFullTypeName, TypeDescription csharpType,
			SortedMap<String, TypeDescription> javaTypes) {
		TypeDescription javaType = javaTypes.get(javaFullTypeName);
		if (javaType == null && ParserUtil.TYPE_ANNOTATION.equals(csharpType.getTypeType())
				&& javaFullTypeName.endsWith("attribute")) {
			javaFullTypeName =
					javaFullTypeName.substring(0, javaFullTypeName.length() - "attribute".length());
			javaType = javaTypes.get(javaFullTypeName);
		}
		return javaType;
	}

	/**
	 * @param result Compare result which is calculated
	 * @param javaType Java type; may not be null
	 * @param csharpType C# type; may not be null
	 */
	private static void compare(CompareResult result, TypeDescription javaType,
			TypeDescription csharpType) {
		if (result == null) {
			throw new IllegalArgumentException("Compare result has to be set!");
		}
		if (javaType == null || csharpType == null) {
			throw new IllegalArgumentException("Type description may not be null!");
		}
		if (ParserUtil.TYPE_DELEGATE.equals(csharpType.getTypeType())) {
			compareCsharpDelegate(result, javaType, csharpType);
		}
		else if (ParserUtil.TYPE_ENUM.equals(csharpType.getTypeType())) {
			compareCsharpEnum(result, javaType, csharpType);
		}
		else if (ParserUtil.TYPE_INTERFACE.equals(csharpType.getTypeType())) {
			compareCsharpInterface(result, javaType, csharpType);
		}
		else if (ParserUtil.TYPE_ANNOTATION.equals(csharpType.getTypeType())) {
			compareCsharpAttribute(result, javaType, csharpType);
		}
		else if (ParserUtil.TYPE_CLASS.equals(csharpType.getTypeType())) {
			compareCsharpClass(result, javaType, csharpType);
		}
		else {
			throw new IllegalArgumentException("Illegal C# type '" + csharpType.getTypeType() + "'!");
		}
	}

	/**
	 * Compare the given C# enum with the given Java type.
	 *
	 * @param result Compare result which is calculated
	 * @param javaType Java type; may not be null
	 * @param csharpType C# type; may not be null
	 */
	private static void compareCsharpEnum(CompareResult result, TypeDescription javaType,
			TypeDescription csharpType) {
		checkModuleName(result, javaType, csharpType);

		boolean success = checkTypeAndGenericParams(result, javaType, csharpType, ParserUtil.TYPE_ENUM,
				"C# enum expects a JAVA enum but was '");
		if (!success) {
			return;
		}

		boolean isEnum = ParserUtil.TYPE_ENUM.equals(javaType.getTypeType());

		List<FieldDescription> remainingJavaFields = new ArrayList<>(javaType.getFieldDescriptions());
		List<String> missingJavaFieldNames = new ArrayList<>(csharpType.getFieldDescriptions().size());
		for (FieldDescription csharpFieldDescription : csharpType.getFieldDescriptions()) {
			List<AnnotationInfo> annotations = csharpFieldDescription.getAnnotations();
			if (!ParserUtil.containsAnnotation(annotations, ParserUtil.JAVA_ANNOTATION_AUTOWIRED)
					&& !ParserUtil.containsAnnotation(annotations, ParserUtil.JAVA_ANNOTATION_LOG_INSTANCE)
					&& !(isEnum && csharpFieldDescription.isEnumConstant())) {
				continue;
			}

			FieldDescription matchingJavaField =
					findMatchingJavaField(csharpFieldDescription, remainingJavaFields);
			if (matchingJavaField == null) {
				missingJavaFieldNames.add(csharpFieldDescription.getName());
			}
			else {
				remainingJavaFields.remove(matchingJavaField);
			}
		}
		String missingFields = StringUtils.EMPTY;
		if (!missingJavaFieldNames.isEmpty()) {
			missingFields = "C# enum has values [" + StringUtils.join(missingJavaFieldNames, ", ")
					+ "] which have no Java counterpart! ";
		}
		if (!remainingJavaFields.isEmpty()) {
			List<String> missingCsharpFieldNames = new ArrayList<>(remainingJavaFields.size());
			for (FieldDescription fieldDescription : remainingJavaFields) {
				missingCsharpFieldNames.add(fieldDescription.getName());
			}
			missingFields += "Java enum has values [" + StringUtils.join(missingCsharpFieldNames, ", ")
					+ "] which have no C# counterpart!";
		}
		if (!StringUtils.isBlank(missingFields)) {
			result.addError(CompareStatus.FIELDS_DIFFER, missingFields);
		}
	}

	/**
	 * Compare the given C# delegate with the given Java type.
	 *
	 * @param result Compare result which is calculated
	 * @param javaType Java type; may not be null
	 * @param csharpType C# type; may not be null
	 */
	private static void compareCsharpDelegate(CompareResult result, TypeDescription javaType,
			TypeDescription csharpType) {
		checkModuleName(result, javaType, csharpType);

		boolean success = checkTypeAndGenericParams(result, javaType, csharpType,
				ParserUtil.TYPE_INTERFACE, "C# delegate expects to match a JAVA interface but was '");
		if (!success) {
			return;
		}

		if (javaType.getMethodDescriptions().size() != 1
				|| csharpType.getMethodDescriptions().size() != 1) {
			result.addError(CompareStatus.PUBLIC_METHOD_COUNT_DIFFERS,
					"C# delegate or the matching JAVA interface has to have ONE method!");
			return; // if this doesn't match no further checks reasonable
		}

		MethodDescription csharpMethodDescription = csharpType.getMethodDescriptions().get(0);
		MethodDescription javaMethodDescription = javaType.getMethodDescriptions().get(0);
		if (!isMethodEqual(result, csharpMethodDescription, javaMethodDescription)) {
			result.addError(CompareStatus.PUBLIC_METHOD_NOT_FOUND,
					"C# and JAVA delegate method differs: \n" + csharpMethodDescription.toString() + "\n"
							+ javaMethodDescription.toString() + "!");
		}
	}

	/**
	 * Compare the given C# interface with the given Java type.
	 *
	 * @param result Compare result which is calculated
	 * @param javaType Java type; may not be null
	 * @param csharpType C# type; may not be null
	 */
	private static void compareCsharpInterface(CompareResult result, TypeDescription javaType,
			TypeDescription csharpType) {
		checkModuleName(result, javaType, csharpType);

		boolean success = checkTypeAndGenericParams(result, javaType, csharpType,
				ParserUtil.TYPE_INTERFACE, "C# interface expects to match a JAVA interface but was '");
		if (!success) {
			return;
		}

		HashSet<MethodDescription> javaMethodDescriptions =
				new HashSet<>(javaType.getMethodDescriptions());
		HashSet<MethodDescription> csharpMethodDescriptions =
				new HashSet<>(csharpType.getMethodDescriptions());

		int javaMethodsSize = javaMethodDescriptions.size();
		int csharpMethodsSize = csharpMethodDescriptions.size();
		if (javaMethodsSize != csharpMethodsSize) {
			result.addError(CompareStatus.PUBLIC_METHOD_COUNT_DIFFERS,
					"C# interface has " + csharpMethodsSize + " method(s) and JAVA interface has "
							+ javaMethodsSize + " method(s)!");
		}

		Iterator<MethodDescription> iter = csharpMethodDescriptions.iterator();
		while (iter.hasNext()) {
			MethodDescription csharpMethodDescription = iter.next();
			MethodDescription javaMethodDescription = findMatchingJavaMethod(result,
					csharpMethodDescription, javaType.getMethodDescriptions(), false);
			if (javaMethodDescription != null) {
				iter.remove();
				javaMethodDescriptions.remove(javaMethodDescription);
			}
		}

		logMissingMethods(result, csharpMethodDescriptions, javaMethodDescriptions, true);
	}

	/**
	 * Compare the given C# attribute with the given Java type.
	 *
	 * @param result Compare result which is calculated
	 * @param javaType Java type; may not be null
	 * @param csharpType C# type; may not be null
	 */
	private static void compareCsharpAttribute(CompareResult result, TypeDescription javaType,
			TypeDescription csharpType) {
		checkModuleName(result, javaType, csharpType);

		boolean success = checkTypeAndGenericParams(result, javaType, csharpType,
				ParserUtil.TYPE_ANNOTATION, "C# attribute expects to match a JAVA annotation but was '");
		if (!success) {
			return;
		}

		int javaMethodsSize = javaType.getMethodDescriptions().size();
		int csharpMethodsSize = csharpType.getMethodDescriptions().size();
		if (javaMethodsSize * 2 != csharpMethodsSize) {
			result.addError(CompareStatus.PUBLIC_METHOD_COUNT_DIFFERS,
					"C# attribute has " + csharpMethodsSize + " method(s) and JAVA annotation has "
							+ javaMethodsSize + " method(s)!");
		}

		List<MethodDescription> javaMethodDescriptions = javaType.getMethodDescriptions();
		List<MethodDescription> cSharpMethodDescriptions = csharpType.getMethodDescriptions();

		List<MethodDescription> missingJavaMethods = new ArrayList<>(csharpMethodsSize);
		List<MethodDescription> missingCsharpMethods = new ArrayList<>(javaMethodsSize);
		for (MethodDescription cSharpMethodDescription : cSharpMethodDescriptions) {
			MethodDescription javaMethodDescription = findMatchingJavaMethod(result,
					cSharpMethodDescription, javaType.getMethodDescriptions(), true);
			if (javaMethodDescription == null) {
				missingJavaMethods.add(cSharpMethodDescription);
			}
		}

		for (MethodDescription javaMethodDescription : javaMethodDescriptions) {
			MethodDescription csharpMethodDescription =
					findMatchingCsharpMethod(result, javaMethodDescription, cSharpMethodDescriptions, true);
			if (csharpMethodDescription == null) {
				missingCsharpMethods.add(javaMethodDescription);
			}
		}

		if (missingJavaMethods.size() == 2 && missingCsharpMethods.size() == 1) {
			// Special case: Java may have a 'value()' property
			checkForAnnotationValueMethod(missingJavaMethods, missingCsharpMethods);
		}

		logMissingMethods(result, missingJavaMethods, missingCsharpMethods);
	}

	/**
	 * Compare the given C# class with the given Java type.
	 *
	 * @param result Compare result which is calculated
	 * @param javaType Java type; may not be null
	 * @param cSharpType C# type; may not be null
	 */
	private static void compareCsharpClass(CompareResult result, TypeDescription javaType,
			TypeDescription cSharpType) {
		checkModuleName(result, javaType, cSharpType);

		boolean success = checkTypeAndGenericParams(result, javaType, cSharpType, ParserUtil.TYPE_CLASS,
				"C# class expects to match a JAVA class but was '");
		if (!success) {
			return;
		}

		LinkedHashMap<String, List<MethodDescription>> nameToCSharpMethodDescriptionMap =
				compileNameToMethodDescriptionMap(cSharpType.getMethodDescriptions());
		LinkedHashMap<String, List<MethodDescription>> nameToJavaMethodDescriptionMap =
				compileNameToMethodDescriptionMap(javaType.getMethodDescriptions());
		LinkedHashMap<String, FieldDescription> nameToJavaFieldDescriptionMap =
				compileNameToFieldDescriptionMap(javaType.getFieldDescriptions());

		handleMethodCounts(javaType.getMethodDescriptions(), cSharpType.getMethodDescriptions());

		// First handle known property patterns
		handleLoggerProperty(result, nameToCSharpMethodDescriptionMap, nameToJavaFieldDescriptionMap);

		handleObjectCollectorSetters(nameToJavaMethodDescriptionMap);

		handleInjectionPoints(result, nameToCSharpMethodDescriptionMap, nameToJavaMethodDescriptionMap,
				nameToJavaFieldDescriptionMap);

		HashSet<MethodDescription> cSharpMethodDescriptions =
				decompileNameToMethodDescriptionMap(nameToCSharpMethodDescriptionMap);
		HashSet<MethodDescription> javaMethodDescriptions =
				decompileNameToMethodDescriptionMap(nameToJavaMethodDescriptionMap);

		Iterator<MethodDescription> iter = cSharpMethodDescriptions.iterator();
		while (iter.hasNext()) {
			MethodDescription cSharpMethodDescription = iter.next();
			MethodDescription javaMethodDescription =
					findMatchingJavaMethod(result, cSharpMethodDescription, javaMethodDescriptions, false);
			if (javaMethodDescription != null) {
				iter.remove();
				javaMethodDescriptions.remove(javaMethodDescription);
			}
		}

		logMissingMethods(result, cSharpMethodDescriptions, javaMethodDescriptions);
	}

	private static LinkedHashMap<String, FieldDescription> compileNameToFieldDescriptionMap(
			List<FieldDescription> fieldDescriptions) {
		LinkedHashMap<String, FieldDescription> nameToFieldDescriptionMap = new LinkedHashMap<>();
		for (FieldDescription fieldDescription : fieldDescriptions) {
			nameToFieldDescriptionMap.put(fieldDescription.getName(), fieldDescription);
		}
		return nameToFieldDescriptionMap;
	}

	private static LinkedHashMap<String, List<MethodDescription>> compileNameToMethodDescriptionMap(
			List<MethodDescription> methodDescriptions) {
		LinkedHashMap<String, List<MethodDescription>> nameToMethodDescriptionMap =
				new LinkedHashMap<>();
		for (MethodDescription methodDescription : methodDescriptions) {
			String methodName = methodDescription.getName();
			List<MethodDescription> list = nameToMethodDescriptionMap.get(methodName);
			if (list == null) {
				list = new ArrayList<>();
				nameToMethodDescriptionMap.put(methodName, list);
			}
			list.add(methodDescription);
		}
		return nameToMethodDescriptionMap;
	}

	private static HashSet<MethodDescription> decompileNameToMethodDescriptionMap(
			LinkedHashMap<String, List<MethodDescription>> nameToMethodDescriptionMap) {
		HashSet<MethodDescription> methodDescriptions = new HashSet<>();
		for (List<MethodDescription> methodDescriptionList : nameToMethodDescriptionMap.values()) {
			methodDescriptions.addAll(methodDescriptionList);
		}
		return methodDescriptions;
	}

	/**
	 * Check if the type of the JAVA type matches the expected one and also compare the number of
	 * generic type parameters. If both match this method returns true, otherwise the CompareResult is
	 * updated and false is returned.
	 *
	 * @param result Compare result which is calculated
	 * @param javaType Java type; may not be null
	 * @param csharpType C# type; may not be null
	 * @param expectedTypeClass Expected type e.g. 'class'
	 * @param infoPrefix Prefix for the additional information text
	 * @return True if the check has passed, false if the CompareResult was changed (status and
	 *         addition information)
	 */
	protected static boolean checkTypeAndGenericParams(CompareResult result, TypeDescription javaType,
			TypeDescription csharpType, String expectedTypeClass, String infoPrefix) {
		if (result == null || javaType == null || csharpType == null || infoPrefix == null) {
			throw new IllegalArgumentException(
					"Mandatory value missing in method 'checkTypeAndGenericParams'!");
		}
		ParserUtil.checkType(expectedTypeClass);

		boolean hasErrors = false;
		// In Java an annotation is an interface, in C# it is a class.
		if (!expectedTypeClass.equals(javaType.getTypeType())) {
			result.addError(CompareStatus.WRONG_TYPE, infoPrefix + javaType.getTypeType() + "'!");
			hasErrors = true;
		}

		if (csharpType.getGenericTypeParams() != javaType.getGenericTypeParams()) {
			// TODO : own enum value for generic?
			result.addError(CompareStatus.PARAMETER_COUNT_DIFFERS,
					"Generic parameter count mismatch (C#: " + csharpType.getGenericTypeParams()
							+ " parameter(s), JAVA: " + javaType.getGenericTypeParams() + " parameter(s))!");
			hasErrors = true;
		}

		return !hasErrors;
	}

	/**
	 * Check if the module names are present and equal, otherwise the CompareResult is updated.
	 *
	 * @param result Compare result which is calculated
	 * @param javaType Java type; may not be null
	 * @param csharpType C# type; may not be null
	 */
	private static void checkModuleName(CompareResult result, TypeDescription javaType,
			TypeDescription csharpType) {
		if (result == null || javaType == null || csharpType == null) {
			throw new IllegalArgumentException("Mandatory value missing in method 'checkModuleName'!");
		}
		boolean isBlank = false;
		String javaModuleName = javaType.getModuleName();
		String csharpModuleName = csharpType.getModuleName();
		if (StringUtils.isBlank(javaModuleName)) {
			result.addError(CompareStatus.NO_MODULENAME_FOUND, "No JAVA module name found!");
			isBlank = true;
		}
		if (StringUtils.isBlank(csharpModuleName)) {
			result.addError(CompareStatus.NO_MODULENAME_FOUND, "No C# module name found!");
			isBlank = true;
		}
		if (!isBlank) {
			if (javaModuleName.startsWith("jambeth")) {
				javaModuleName = StringUtils.replaceOnce(javaModuleName, "jambeth", "ambeth");
			}
			// Java module names are based on typical maven project names. So we have to "adjust" the C#
			// module name.
			csharpModuleName = csharpModuleName.replace('.', '-').toLowerCase();
			if (!javaModuleName.equals(csharpModuleName)) {
				if (javaModuleName.equalsIgnoreCase(csharpModuleName)) {
					result.addError(CompareStatus.MODULENAME_CASE, "C# module name '" + csharpModuleName
							+ "' differs from the JAVA module name '" + javaModuleName + "!");
				}
				else {
					result.addError(CompareStatus.MODULENAME_DIFFERS, "C# module name '" + csharpModuleName
							+ "' differs from the JAVA module name '" + javaModuleName + "!");
				}
			}
		}
	}

	// /**
	// * @param methodDescriptions
	// * List of MethodDescription entities
	// * @param modifierCategories
	// * List of modifiers to be count
	// * @return Text with the counts or null
	// */
	// private static String
	// getMethodCountTextByModifier(List<MethodDescription> methodDescriptions,
	// String[]
	// modifierCategories) {
	// Map<String, Integer> map = getMethodCountByModifier(methodDescriptions,
	// modifierCategories);
	// return getMethodCountText(modifierCategories, map);
	// }

	// /**
	// * @param methodDescriptions
	// * List of MethodDescription entities
	// * @param modifierCategories
	// * List of modifiers to be count
	// * @return Map with the modifier (from modifierCategories) as key and the
	// count as value
	// */
	// private static Map<String, Integer>
	// getMethodCountByModifier(List<MethodDescription> methodDescriptions,
	// String[]
	// modifierCategories) {
	// if (modifierCategories == null || modifierCategories.length == 0) {
	// return null;
	// }
	// Map<String, Integer> map = new HashMap<String, Integer>();
	// for (String category : modifierCategories) {
	// map.put(category, Integer.valueOf(0));
	// }
	// for (MethodDescription methodDescription : methodDescriptions) {
	// List<String> modifiers = methodDescription.getModifiers();
	// for (String modifier : modifiers) {
	// Integer count = map.get(modifier);
	// if (count != null) {
	// count++;
	// map.put(modifier, count);
	// }
	// }
	// }
	// return map;
	// }

	// /**
	// * @param modifierCategories
	// * List of modifiers to be count
	// * @param map
	// * Map with the counts
	// * @return Text with the counts or null
	// */
	// private static String getMethodCountText(String[] modifierCategories,
	// Map<String, Integer> map) {
	// if (map == null) {
	// return null;
	// }
	// List<String> infos = new ArrayList<String>();
	// for (String category : modifierCategories) {
	// Integer count = map.get(category);
	// infos.add(count + " " + category);
	// }
	// return StringUtils.join(infos, ", ");
	// }

	/**
	 * Find a matching Java method.
	 *
	 * @param csharpMethodDescription C# method
	 * @param nameToJavaMethodDescriptionMap A list of Java methods to search in
	 * @param isAttribute Declaring type is Attribute
	 * @return Java MethodDescription or null
	 */
	private static MethodDescription findMatchingJavaMethod(CompareResult result,
			MethodDescription csharpMethodDescription,
			Collection<MethodDescription> javaMethodDescriptions, boolean isAttribute) {
		if (csharpMethodDescription == null || javaMethodDescriptions == null) {
			return null;
		}
		for (MethodDescription javaMethodDescription : javaMethodDescriptions) {
			if (!isAttribute) {
				if (isMethodEqual(result, csharpMethodDescription, javaMethodDescription)) {
					return javaMethodDescription;
				}
			}
			else {
				if (isAnnotationMethodEqual(csharpMethodDescription, javaMethodDescription)) {
					return javaMethodDescription;
				}
			}
		}
		return null;
	}

	/**
	 * Find a matching C# method.
	 *
	 * @param javaMethodDescription Java method
	 * @param csharpMethods A list of C# methods to search in
	 * @param isAnnotation Declaring class is Annotation
	 * @return C# MethodDescription or null
	 */
	private static MethodDescription findMatchingCsharpMethod(CompareResult result,
			MethodDescription javaMethodDescription, List<MethodDescription> csharpMethods,
			boolean isAnnotation) {
		if (javaMethodDescription != null && csharpMethods != null) {
			for (MethodDescription csharpMethodDescription : csharpMethods) {
				if (!isAnnotation) {
					if (isMethodEqual(result, csharpMethodDescription, javaMethodDescription)) {
						return csharpMethodDescription;
					}
				}
				else {
					if (isAnnotationMethodEqual(csharpMethodDescription, javaMethodDescription)) {
						return csharpMethodDescription;
					}
				}
			}
		}
		return null;
	}

	/**
	 * @param csharpMethodDescription C# method
	 * @param javaMethodDescription Java method
	 * @return True if the methods are equal
	 */
	protected static boolean isMethodEqual(CompareResult result,
			MethodDescription csharpMethodDescription, MethodDescription javaMethodDescription) {
		if (!isMethodNameEqual(result, csharpMethodDescription, javaMethodDescription)) {
			return false;
		}
		if (!isTypeMatch(csharpMethodDescription.getReturnType(),
				javaMethodDescription.getReturnType())) {
			return false;
		}
		if (!areMethodParameterTypesEquivalent(csharpMethodDescription.getParameterTypes(),
				javaMethodDescription.getParameterTypes())) {
			return false;
		}
		if (!areMethodModifiersEquivalent(csharpMethodDescription.getModifiers(),
				javaMethodDescription.getModifiers())) {
			return false;
		}
		return true;
	}

	/**
	 * Checks if the methods are equal and account for declaring type being an annotation.
	 *
	 * @param cSharpMethodDescription C# method
	 * @param javaMethodDescription Java method
	 * @param valueCase
	 * @return True if the methods are equal
	 */
	protected static boolean isAnnotationMethodEqual(MethodDescription cSharpMethodDescription,
			MethodDescription javaMethodDescription) {
		String cSharpMethodName = cSharpMethodDescription.getName();
		// Annotations have only property accessors.
		if (!cSharpMethodName.startsWith("set_") && !cSharpMethodName.startsWith("get_")) {
			return false;
		}
		String javaMethodName = javaMethodDescription.getName();
		String modifiedCSharpMethodName = cSharpMethodName.substring(4);
		String modifiedJavaMethodName = firstToUpperCase(javaMethodName);
		if (!modifiedCSharpMethodName.equals(modifiedJavaMethodName)) {
			return false;
		}
		String returnType = cSharpMethodDescription.getReturnType();
		List<String> parameterTypes = cSharpMethodDescription.getParameterTypes();
		if (cSharpMethodName.startsWith("set_")) {
			// Annotations have no setters, but if there is a matching getter it is ok.
			parameterTypes = cSharpMethodDescription.getParameterTypes();
			if (parameterTypes.size() != 1) {
				return false;
			}
			returnType = parameterTypes.get(0);
			if (!"void".equals(cSharpMethodDescription.getReturnType())) {
				return false;
			}
			parameterTypes = Collections.emptyList();
		}
		else {
			if (!areMethodModifiersEquivalent(cSharpMethodDescription.getModifiers(),
					javaMethodDescription.getModifiers())) {
				return false;
			}
		}
		String javaReturnType = javaMethodDescription.getReturnType();
		if (!isAnnotationValueTypeMatch(returnType, javaReturnType)) {
			return false;
		}
		if (!areMethodParameterTypesEquivalent(parameterTypes,
				javaMethodDescription.getParameterTypes())) {
			return false;
		}
		return true;
	}

	/**
	 * Checks a C# attribute property against one Java annotation method for a match.
	 *
	 * @param cSharpMethods C# property methods
	 * @param javaMethods Java method
	 */
	protected static void checkForAnnotationValueMethod(List<MethodDescription> cSharpMethods,
			List<MethodDescription> javaMethods) {
		if (cSharpMethods.size() != 2 || javaMethods.size() != 1) {
			return;
		}

		MethodDescription cSharpGetter;
		MethodDescription cSharpSetter;
		MethodDescription javaGetter = javaMethods.get(0);

		if (!javaGetter.getName().equals("value")) {
			return;
		}
		if (!checkModifiersOnly(javaGetter.getModifiers(), ParserUtil.MODIFIER_PUBLIC,
				ParserUtil.MODIFIER_ABSTRACT)) {
			return;
		}
		if (!javaGetter.getParameterTypes().isEmpty()) {
			return;
		}
		String javaReturnType = javaGetter.getReturnType();
		if (javaReturnType.equals("void")) {
			return;
		}

		MethodDescription temp = cSharpMethods.get(0);
		if (temp.getName().startsWith("get_")) {
			cSharpGetter = temp;
			cSharpSetter = cSharpMethods.get(1);
		}
		else {
			cSharpGetter = cSharpMethods.get(1);
			cSharpSetter = temp;
		}

		String propertyName = cSharpGetter.getName().substring(4);
		if (!cSharpSetter.getName().equals("set_" + propertyName)) {
			return;
		}

		if (!checkModifiersOnly(cSharpGetter.getModifiers(), ParserUtil.MODIFIER_PUBLIC)) {
			return;
		}
		if (!cSharpGetter.getParameterTypes().isEmpty()) {
			return;
		}
		String cSharpReturnType = cSharpGetter.getReturnType();
		if (!isAnnotationValueTypeMatch(cSharpReturnType, javaReturnType)) {
			return;
		}

		if (!checkModifiersOnly(cSharpSetter.getModifiers(), ParserUtil.MODIFIER_PRIVATE)) {
			return;
		}
		List<String> cSharpSetterParameterTypes = cSharpSetter.getParameterTypes();
		if (cSharpSetterParameterTypes.size() != 1
				|| !cSharpSetterParameterTypes.get(0).equals(cSharpReturnType)) {
			return;
		}
		if (!cSharpSetter.getReturnType().equals(TYPE_VOID)) {
			return;
		}

		cSharpMethods.clear();
		javaMethods.clear();
	}

	/**
	 * @param csharpMethodDescription C# method; mandatory
	 * @param javaMethodDescription Java method; mandatory
	 * @return True if the method names are equal
	 */
	protected static boolean isMethodNameEqual(CompareResult result,
			MethodDescription csharpMethodDescription, MethodDescription javaMethodDescription) {
		if (csharpMethodDescription == null || javaMethodDescription == null) {
			throw new IllegalArgumentException("Method description missing!");
		}

		String csharpMethodName = csharpMethodDescription.getName();
		String javaMethodName = javaMethodDescription.getName();
		if (csharpMethodName.equals(firstToUpperCase(javaMethodName))) {
			if (!javaMethodName.equals(firstToLowerCase(csharpMethodName))) {
				result.addError(CompareStatus.METHOD_NAME_CASE,
						"C#: '" + csharpMethodName + "', Java: '" + javaMethodName + "'");
			}
			return true;
		}

		// Handle property spelling
		if (csharpMethodName.startsWith("get_") || csharpMethodName.startsWith("set_")) {
			String adaptedCsharpMethodName =
					StringUtils.replaceOnce(csharpMethodName, "_", StringUtils.EMPTY);
			if (adaptedCsharpMethodName.equals(javaMethodName)) {
				return true;
			}
		}

		// Handle boolean property spelling
		if (csharpMethodDescription.getReturnType().startsWith("bool")
				&& csharpMethodName.startsWith("get_")) {
			String adaptedCsharpMethodName = StringUtils.replaceOnce(csharpMethodName, "get_", "is");
			if (adaptedCsharpMethodName.equals(javaMethodName)) {
				return true;
			}
			adaptedCsharpMethodName = StringUtils.replaceOnce(csharpMethodName, "get_Is", "is");
			if (adaptedCsharpMethodName.equals(javaMethodName)) {
				return true;
			}
			adaptedCsharpMethodName = StringUtils.replaceOnce(csharpMethodName, "get_Has", "has");
			if (adaptedCsharpMethodName.equals(javaMethodName)) {
				return true;
			}
		}
		List<String> parameterTypes = csharpMethodDescription.getParameterTypes();
		if (parameterTypes.size() == 1 && parameterTypes.get(0).startsWith("bool")
				&& csharpMethodName.startsWith("set_")) {
			String adaptedCsharpMethodName = StringUtils.replaceOnce(csharpMethodName, "set_Is", "set");
			if (adaptedCsharpMethodName.equals(javaMethodName)) {
				return true;
			}
		}

		// Special case hash code
		if (csharpMethodName.equals("GetHashCode") && javaMethodName.equals("hashCode")) {
			return true;
		}

		if (csharpMethodName.equalsIgnoreCase(firstToUpperCase(javaMethodName))) {
			result.addError(CompareStatus.METHOD_NAME_CASE,
					"C#: '" + csharpMethodName + "', Java: '" + javaMethodName + "'");
			return true;
		}

		return false;
	}

	/**
	 * @param csharpMethodModifiers List of C# method modifiers
	 * @param javaMethodModifiers List of JAVA method modifiers
	 * @return True if the method modifiers are equivalent
	 */
	protected static boolean areMethodModifiersEquivalent(List<String> csharpMethodModifiers,
			List<String> javaMethodModifiers) {
		if (csharpMethodModifiers.isEmpty() && javaMethodModifiers.isEmpty()) {
			return true;
		}
		List<String> exactMatchModifiers =
				Arrays.asList("public", "protected", "private", "static", "abstract");
		for (String csharpModifier : csharpMethodModifiers) {
			if (exactMatchModifiers.contains(csharpModifier)) {
				// The JAVA modifier has to match exactly
				if (!javaMethodModifiers.contains(csharpModifier)) {
					return false;
				}
			}
			else if ("internal".equals(csharpModifier)) {
				// C# has "internal protected" which means internal OR protected
				// -> check only if not protected
				if (!csharpMethodModifiers.contains("protected")) {
					// Package private in JAVA has no own modifier -> check
					// discrepance
					if (javaMethodModifiers.contains("public") || javaMethodModifiers.contains("protected")
							|| javaMethodModifiers.contains("private")) {
						return false;
					}
				}
			}
		}

		return true;
	}

	/**
	 * Check method parameter types. The sequence of the parameters and the types itself have to
	 * match.
	 *
	 * @param csharpMethodParameterTypes List of C# method parameter types
	 * @param javaMethodParameterTypes List of JAVA method parameter types
	 * @return True if the method parameter types are equivalent
	 */
	protected static boolean areMethodParameterTypesEquivalent(
			List<String> csharpMethodParameterTypes, List<String> javaMethodParameterTypes) {
		if (csharpMethodParameterTypes.isEmpty() && javaMethodParameterTypes.isEmpty()) {
			return true;
		}

		List<String> modifiedCSharpMethodParameterTypes = new ArrayList<>(csharpMethodParameterTypes);
		List<String> modifiedJavaMethodParameterTypes = new ArrayList<>(javaMethodParameterTypes);

		// FIXME Better handling for parameter differences
		modifiedJavaMethodParameterTypes
				.remove("com.koch.ambeth.util.objectcollector.IObjectCollector");

		int csharpParamCount = modifiedCSharpMethodParameterTypes.size();
		int javaParamCount = modifiedJavaMethodParameterTypes.size();
		if (csharpParamCount != javaParamCount) {
			return false;
		}

		for (int i = 0; i < csharpParamCount; i++) {
			String csharpParamType = modifiedCSharpMethodParameterTypes.get(i);
			String javaParamType = modifiedJavaMethodParameterTypes.get(i);
			if (!isTypeMatch(csharpParamType, javaParamType)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Find a matching Java field.
	 *
	 * @param csharpFieldDescription C# field
	 * @param javaFields A list of Java fields to search in
	 * @return Java FieldDescription or null
	 */
	private static FieldDescription findMatchingJavaField(FieldDescription csharpFieldDescription,
			List<FieldDescription> javaFields) {
		if (csharpFieldDescription != null && javaFields != null) {
			for (FieldDescription javaFieldDescription : javaFields) {
				if (isFieldNameEqual(csharpFieldDescription, javaFieldDescription)) {
					return javaFieldDescription;
				}
			}
		}
		return null;
	}

	/**
	 * @param csharpFieldDescription C# field
	 * @param javaFieldDescription Java field
	 * @return True if the field names are equal
	 */
	private static boolean isFieldNameEqual(FieldDescription csharpFieldDescription,
			FieldDescription javaFieldDescription) {
		return csharpFieldDescription.getName().equals(javaFieldDescription.getName());
	}

	/**
	 * @param csharpType C# type
	 * @param javaType Java type
	 * @return True if the types are equal
	 */
	protected static boolean isTypeMatch(String csharpType, String javaType) {
		return isTypeMatch(csharpType, javaType, 0);
	}

	/**
	 * @param csharpType C# type
	 * @param javaType Java type
	 * @param recursionCount Counter which counts the recursion depth
	 * @return True if the types are equal
	 */
	protected static boolean isTypeMatch(String csharpType, String javaType, int recursionCount) {
		if (recursionCount > 10) {
			throw new IllegalArgumentException("Problems analyzing a type. Recursion depth exceeded!");
		}

		String originalCSharpType = csharpType;
		String originalJavaType = javaType;

		while (csharpType.endsWith("[]") && javaType.endsWith("[]")) {
			csharpType = csharpType.substring(0, csharpType.length() - 2);
			javaType = javaType.substring(0, javaType.length() - 2);
		}

		String originalNonArrayCSharpType = csharpType;
		String originalNonArrayJavaType = javaType;

		// At the moment we don't look at the parameterized types e.g.
		// IDictionary<string, IList<string>>
		if (!csharpType.startsWith("System.Nullable")) {
			int csharpIndex = csharpType.indexOf("<");
			if (csharpIndex > 0) {
				csharpType = csharpType.substring(0, csharpIndex);
			}
		}
		int javaIndex = javaType.indexOf("<");
		if (javaIndex > 0) {
			javaType = javaType.substring(0, javaIndex);
		}

		// Compare type e.g. "void" or "int" or full type names but exclude C#
		// byte which doesn't match the JAVA byte
		// TODO 'byte' klaeren
		if (/* !"byte".equals(csharpType) && */csharpType.equalsIgnoreCase(javaType)) {
			return true;
		}

		// Simple language types
		if ("bool".equals(csharpType) && "boolean".equals(javaType) || //
				"System.Nullable<bool>".equals(csharpType) && "java.lang.Boolean".equals(javaType) || //
				"bool?".equals(csharpType) && "java.lang.Boolean".equals(javaType) || //
				"System.Nullable<char>".equals(csharpType) && "java.lang.Character".equals(javaType) || //
				"char?".equals(csharpType) && "java.lang.Character".equals(javaType) || //
				"System.Nullable<byte>".equals(csharpType) && "java.lang.Byte".equals(javaType) || //
				"sbyte".equals(csharpType) && "byte".equals(javaType) || //
				"sbyte?".equals(csharpType) && "java.lang.Byte".equals(javaType) || //
				"System.Nullable<short>".equals(csharpType) && "java.lang.Short".equals(javaType) || //
				"short?".equals(csharpType) && "java.lang.Short".equals(javaType) || //
				"System.Nullable<int>".equals(csharpType) && "java.lang.Integer".equals(javaType) || //
				"int?".equals(csharpType) && "java.lang.Integer".equals(javaType) || //
				"System.Nullable<long>".equals(csharpType) && "java.lang.Long".equals(javaType) || //
				"long?".equals(csharpType) && "java.lang.Long".equals(javaType) || //
				"single".equals(csharpType) && "float".equals(javaType) || //
				"System.Nullable<single>".equals(csharpType) && "java.lang.Float".equals(javaType) || //
				"single?".equals(csharpType) && "java.lang.Float".equals(javaType) || //
				"System.Nullable<double>".equals(csharpType) && "java.lang.Double".equals(javaType) || //
				"double?".equals(csharpType) && "java.lang.Double".equals(javaType) || //
				"System.Nullable<decimal>".equals(csharpType) && "java.math.BigDecimal".equals(javaType) || //
				"decimal?".equals(csharpType) && "java.math.BigDecimal".equals(javaType)) {
			return true;
		}

		// TODO: C# - what about sbyte, uint, ushort, ulong, decimal (not
		// nullable)

		// Equivalent types
		HashSet<String> knownCSharpType = JAVA_CLASS_TO_CSHARP_TYPES.get(javaType);
		if (knownCSharpType != null && knownCSharpType.contains(csharpType)) {
			return true;
		}
		knownCSharpType = JAVA_CLASS_TO_CSHARP_TYPES.get(originalNonArrayJavaType);
		if (knownCSharpType != null && knownCSharpType.contains(originalNonArrayCSharpType)) {
			return true;
		}
		knownCSharpType = JAVA_CLASS_TO_CSHARP_TYPES.get(originalJavaType);
		if (knownCSharpType != null && knownCSharpType.contains(originalCSharpType)) {
			return true;
		}

		return false;
	}

	private static boolean isAnnotationValueTypeMatch(String cSharpType, String javaType) {
		if (isTypeMatch(cSharpType, javaType)) {
			return true;
		}
		// C# allows to use the same Attribute multiple times. In Java this can be compensated by using
		// not the same
		// value type but an array of it.
		if (javaType.endsWith("[]")) {
			javaType = javaType.substring(0, javaType.length() - 2);
			if (isTypeMatch(cSharpType, javaType)) {
				return true;
			}
		}
		return false;
	}

	private static void handleMethodCounts(List<MethodDescription> javaMethodsCopy,
			List<MethodDescription> cSharpMethodsCopy) {
		int javaMethodsSize = javaMethodsCopy.size();
		int csharpMethodsSize = cSharpMethodsCopy.size();

		if (javaMethodsSize != csharpMethodsSize) {
			// String[] modifierCategories = new String[] { "public",
			// "protected" };
			// Map<String, Integer> csharpMap =
			// getMethodCountByModifier(csharpMethodDescriptions,
			// modifierCategories);
			// Map<String, Integer> javaMap =
			// getMethodCountByModifier(javaMethodDescriptions,
			// modifierCategories);
			// final CompareError compareError;
			// if (csharpMap.get("public").intValue() !=
			// javaMap.get("public").intValue()) {
			// compareError = new
			// CompareError(CompareStatus.PUBLIC_METHOD_COUNT_DIFFERS);
			// }
			// else if (csharpMap.get("protected").intValue() !=
			// javaMap.get("protected").intValue()) {
			// compareError = new
			// CompareError(CompareStatus.PROTECTED_METHOD_COUNT_DIFFERS);
			// }
			// else {
			// compareError = new
			// CompareError(CompareStatus.METHOD_COUNT_DIFFERS);
			// }
			// String csCount = getMethodCountText(modifierCategories,
			// csharpMap);
			// if (!StringUtils.isBlank(csCount)) {
			// csCount = " (" + csCount + ")";
			// }
			// String javaCount = getMethodCountText(modifierCategories,
			// javaMap);
			// if (!StringUtils.isBlank(javaCount)) {
			// javaCount = " (" + javaCount + ")";
			// }
			// compareError.setAdditionalInformation("C# class has " +
			// csharpMethodsSize + " method(s)" + csCount +
			// " and JAVA class has " + javaMethodsSize
			// + " method(s)" + javaCount + "!");
			// result.addError(compareError);
		}
	}

	/**
	 * Special case handling for the logger property. Checks for correct logger property
	 * implementation and a matching field in the Java methods.
	 *
	 * @param result Compare result which is calculated
	 * @param nameToCSharpMethodDescriptionMap Method name to MethodDescription map of all remaining
	 *        C# methods.
	 * @param nameToJavaFieldDescriptionMap Field name to FieldDescription map of all Java fields.
	 */
	protected static void handleLoggerProperty(CompareResult result,
			Map<String, List<MethodDescription>> nameToCSharpMethodDescriptionMap,
			Map<String, FieldDescription> nameToJavaFieldDescriptionMap) {
		MethodDescription getter =
				getUniqueMethod(nameToCSharpMethodDescriptionMap, LOG_PROPERTY_GETTER_NAME);
		MethodDescription setter =
				getUniqueMethod(nameToCSharpMethodDescriptionMap, LOG_PROPERTY_SETTER_NAME);
		FieldDescription field = nameToJavaFieldDescriptionMap.get(LOG_PROPERTY_FIELD_NAME);

		ArrayList<String> violationMessages = new ArrayList<>();

		if (getter == null || setter == null || field == null) {
			return;
		}

		nameToCSharpMethodDescriptionMap.remove(LOG_PROPERTY_GETTER_NAME);
		nameToCSharpMethodDescriptionMap.remove(LOG_PROPERTY_SETTER_NAME);
		nameToJavaFieldDescriptionMap.remove(LOG_PROPERTY_FIELD_NAME);

		if (!LOG_TYPE_CSHARP.equals(getter.getReturnType())) {
			violationMessages.add("Wrong return type for logger property getter.");
		}
		if (!getter.getParameterTypes().isEmpty()) {
			violationMessages.add("Logger property getter has parameter(s).");
		}
		if (!checkModifiersOnly(getter.getModifiers(), ParserUtil.MODIFIER_PRIVATE)) {
			violationMessages.add("Wrong modifier(s) for logger property getter.");
		}
		if (!violationMessages.isEmpty()) {
			result.addError(CompareStatus.PATTERN_VIOLATION,
					StringUtils.join(violationMessages, " ") + " - \n" + getter + "!");
			violationMessages.clear();
		}

		if (!TYPE_VOID.equals(setter.getReturnType())) {
			violationMessages.add("Wrong return type for logger property setter.");
		}
		List<String> parameterTypes = setter.getParameterTypes();
		if (parameterTypes.size() != 1 || !LOG_TYPE_CSHARP.equals(parameterTypes.get(0))) {
			violationMessages.add("Logger setter has wrong parameter(s).");
		}
		if (!checkModifiersOnly(setter.getModifiers(), ParserUtil.MODIFIER_PUBLIC)) {
			violationMessages.add("Wrong modifiers for logger property setter.");
		}
		if (!violationMessages.isEmpty()) {
			result.addError(CompareStatus.PATTERN_VIOLATION,
					StringUtils.join(violationMessages, " ") + " - \n" + setter + "!");
			violationMessages.clear();
		}

		if (!LOG_TYPE_JAVA.equals(field.getFieldType())) {
			violationMessages.add("Wrong type for logger field.");
		}
		if (!checkModifiersOnly(field.getModifiers(), ParserUtil.MODIFIER_PRIVATE)) {
			violationMessages.add("Wrong modifier(s) for logger field.");
		}
		if (!violationMessages.isEmpty()) {
			result.addError(CompareStatus.PATTERN_VIOLATION,
					StringUtils.join(violationMessages, " ") + " - \n" + field + "!");
		}
	}

	/**
	 * Special case handling for injection points. An injection is a special property. It is
	 * characterized by a C# property with a public setter and a protected getter and a public Java
	 * setter or private field annotated with "@Autowired".
	 *
	 * @param result Compare result which is calculated
	 * @param nameToCSharpMethodDescriptionMap Method name to MethodDescription map of all remaining
	 *        C# methods.
	 * @param nameToJavaMethodDescriptionMap Method name to MethodDescription map of all remaining
	 *        Java methods.
	 * @param nameToJavaFieldDescriptionMap Field name to FieldDescription map of all Java fields.
	 */
	protected static void handleInjectionPoints(CompareResult result,
			Map<String, List<MethodDescription>> nameToCSharpMethodDescriptionMap,
			Map<String, List<MethodDescription>> nameToJavaMethodDescriptionMap,
			Map<String, FieldDescription> nameToJavaFieldDescriptionMap) {
		HashSet<String> alreadyHandled = new HashSet<>();
		Iterator<Entry<String, List<MethodDescription>>> iter =
				nameToCSharpMethodDescriptionMap.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, List<MethodDescription>> cSharpMethodEntry = iter.next();
			String cSharpMethodName = cSharpMethodEntry.getKey();
			MethodDescription cSharpMethodDescription = getUniqueMethod(cSharpMethodEntry.getValue());
			if (cSharpMethodDescription == null) {
				// Handling of non-unique getters/setters is to complex at the moment
				continue;
			}
			if (alreadyHandled.contains(cSharpMethodName)) {
				iter.remove();
				continue;
			}

			MethodDescription cSharpGetter;
			MethodDescription cSharpSetter;
			String propertyName;
			String otherCSharpMethodName;
			Matcher matcher;
			if ((matcher = CSHARP_GETTER_NAME.matcher(cSharpMethodName)).matches()) {
				propertyName = matcher.group(1);
				otherCSharpMethodName = "set_" + propertyName;
				if (propertyName.startsWith("Is")) {
					propertyName = propertyName.substring(2);
				}
				cSharpGetter = cSharpMethodDescription;
				cSharpSetter = getUniqueMethod(nameToCSharpMethodDescriptionMap, otherCSharpMethodName);
			}
			else if ((matcher = CSHARP_SETTER_NAME.matcher(cSharpMethodName)).matches()) {
				propertyName = matcher.group(1);
				otherCSharpMethodName = "get_" + propertyName;
				if (propertyName.startsWith("Is")) {
					propertyName = propertyName.substring(2);
				}
				cSharpGetter = getUniqueMethod(nameToCSharpMethodDescriptionMap, otherCSharpMethodName);
				cSharpSetter = cSharpMethodDescription;
			}
			else {
				continue;
			}

			MethodDescription javaGetter =
					getUniqueMethod(nameToJavaMethodDescriptionMap, "get" + propertyName);
			if (javaGetter == null) {
				javaGetter = getUniqueMethod(nameToJavaMethodDescriptionMap, "is" + propertyName);
			}
			MethodDescription javaSetter =
					getUniqueMethod(nameToJavaMethodDescriptionMap, "set" + propertyName);
			FieldDescription javaField = null;

			if (cSharpGetter == null || cSharpSetter == null) {
				continue;
			}
			List<String> definingJavaAnnotations;
			String javaType;
			if (javaSetter != null) {
				List<AnnotationInfo> annotations = javaSetter.getAnnotations();
				definingJavaAnnotations = toAnnotationTypeNames(annotations);
				List<String> parameterTypes = javaSetter.getParameterTypes();
				if (parameterTypes.size() == 1) {
					javaType = parameterTypes.get(0);
				}
				else {
					continue;
				}
			}
			else {
				// Check for field injection
				String fieldName = firstToLowerCase(propertyName);
				javaField = nameToJavaFieldDescriptionMap.get(fieldName);
				if (javaField == null) {
					continue;
				}
				List<AnnotationInfo> annotations = javaField.getAnnotations();
				definingJavaAnnotations = toAnnotationTypeNames(annotations);
				javaType = javaField.getFieldType();
			}

			// Checking C# setter
			List<String> cSharpSetterModifiers = cSharpSetter.getModifiers();
			if (!checkCSharpInjectionPointModifiers(cSharpSetterModifiers)) {
				continue;
			}
			if (!TYPE_VOID.equals(cSharpSetter.getReturnType())) {
				continue;
			}
			List<String> cSharpSetterParameterTypes = cSharpSetter.getParameterTypes();
			if (cSharpSetterParameterTypes.size() != 1) {
				continue;
			}
			String cSharpType = cSharpSetterParameterTypes.get(0);

			if (!definingJavaAnnotations.contains(ParserUtil.JAVA_ANNOTATION_PROPERTY)
					&& NOT_INJECTABLE_JAVA_TYPES.contains(javaType)) {
				// Not an Injection point.
				continue;
			}

			// Checking Java setter
			if (javaSetter != null) {
				if (!isMethodEqual(result, cSharpSetter, javaSetter)) {
					continue;
				}
			}
			else {
				// If the Java setter is null, the Java field has to be present.
				List<String> javeFieldModifiers = javaField.getModifiers();
				if (!isTypeMatch(cSharpType, javaField.getFieldType())) {
					continue;
				}
				if (javeFieldModifiers.size() != 1) {
					continue;
				}
				if (!javeFieldModifiers.contains(ParserUtil.MODIFIER_PROTECTED)) {
					result.addError(CompareStatus.PATTERN_VIOLATION, "Autowired field '" + javaField.getName()
							+ "' should have the visibility 'protected'.");
				}
			}

			if (javaGetter != null) {
				List<String> javeGetterModifiers = javaGetter.getModifiers();
				if (javeGetterModifiers.size() != 1
						|| !(javeGetterModifiers.contains(ParserUtil.MODIFIER_PUBLIC)
								|| javeGetterModifiers.contains(ParserUtil.MODIFIER_PROTECTED))) {
					continue;
				}
				if (!isTypeMatch(cSharpType, javaGetter.getReturnType())) {
					continue;
				}
				if (!javaGetter.getParameterTypes().isEmpty()) {
					continue;
				}
			}

			if (!cSharpType.equals(cSharpGetter.getReturnType())) {
				continue;
			}
			if (!cSharpGetter.getParameterTypes().isEmpty()) {
				continue;
			}
			List<String> cSharpGetterModifiers = cSharpGetter.getModifiers();
			String expectedVisibility =
					javaGetter == null ? ParserUtil.MODIFIER_PROTECTED : javaGetter.getModifiers().get(0);
			if (!checkCSharpInjectionPointModifiers(cSharpGetterModifiers, expectedVisibility)) {
				result.addError(CompareStatus.PATTERN_VIOLATION,
						"C# injection point getter '" + cSharpGetter.getName()
								+ "' should have the visibility 'protected' or match the Java getter.");
			}

			iter.remove();
			alreadyHandled.add(otherCSharpMethodName);
			removeIfNotNull(nameToJavaMethodDescriptionMap, javaSetter);
			removeIfNotNull(nameToJavaMethodDescriptionMap, javaGetter);
			removeIfNotNull(nameToJavaFieldDescriptionMap, javaField);
		}
	}

	private static List<String> toAnnotationTypeNames(List<AnnotationInfo> annotations) {
		List<String> definingJavaAnnotations;
		definingJavaAnnotations = new ArrayList<>(annotations.size());
		for (AnnotationInfo annotation : annotations) {
			String annotationType = annotation.getAnnotationType();
			definingJavaAnnotations.add(annotationType);
		}
		return definingJavaAnnotations;
	}

	protected static void handleObjectCollectorSetters(
			Map<String, List<MethodDescription>> nameToJavaMethodDescriptionMap) {
		List<MethodDescription> setters = nameToJavaMethodDescriptionMap.get("setObjectCollector");
		if (setters == null) {
			return;
		}

		for (int i = setters.size(); i-- > 0;) {
			MethodDescription setter = setters.get(i);
			if (!setter.getReturnType().equals(TYPE_VOID)) {
				continue;
			}
			if (!checkModifiersOnly(setter.getModifiers(), ParserUtil.MODIFIER_PUBLIC)) {
				continue;
			}
			List<String> parameterTypes = setter.getParameterTypes();
			if (parameterTypes.size() != 1
					|| !parameterTypes.contains("com.koch.ambeth.util.objectcollector.IObjectCollector")
							&& !parameterTypes
									.contains("com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector")) {
				continue;
			}
			setters.remove(i);
			break;
		}
		if (setters.isEmpty()) {
			nameToJavaMethodDescriptionMap.remove("setObjectCollector");
		}
	}

	private static boolean checkModifiersOnly(List<String> objectsModifiers, String... modifiers) {
		if (objectsModifiers.size() != modifiers.length) {
			return false;
		}
		for (String modifier : modifiers) {
			if (!objectsModifiers.contains(modifier)) {
				return false;
			}
		}
		return true;
	}

	private static MethodDescription getUniqueMethod(
			Map<String, List<MethodDescription>> nameToMethodDescriptionMap, String methodName) {
		List<MethodDescription> list = nameToMethodDescriptionMap.get(methodName);
		return getUniqueMethod(list);
	}

	private static MethodDescription getUniqueMethod(List<MethodDescription> list) {
		if (list != null && list.size() == 1) {
			return list.get(0);
		}
		return null;
	}

	private static boolean checkCSharpInjectionPointModifiers(List<String> modifiers) {
		return checkCSharpInjectionPointModifiers(modifiers, ParserUtil.MODIFIER_PUBLIC);
	}

	private static boolean checkCSharpInjectionPointModifiers(List<String> modifiers,
			String expectedVisibility) {
		if (checkModifiersOnly(modifiers, expectedVisibility)) {
			return true;
		}
		if (checkModifiersOnly(modifiers, expectedVisibility, ParserUtil.MODIFIER_VIRTUAL)) {
			return true;
		}
		return false;
	}

	private static void removeIfNotNull(Map<String, ?> nameToJavaDescriptionMap,
			INamed namedElement) {
		if (namedElement != null) {
			nameToJavaDescriptionMap.remove(namedElement.getName());
		}
	}

	private static void logMissingMethods(CompareResult result,
			Collection<MethodDescription> cSharpMethodDescriptions,
			Collection<MethodDescription> javaMethodDescriptions) {
		logMissingMethods(result, cSharpMethodDescriptions, javaMethodDescriptions, false);
	}

	private static void logMissingMethods(CompareResult result,
			Collection<MethodDescription> cSharpMethodDescriptions,
			Collection<MethodDescription> javaMethodDescriptions, boolean isInterface) {
		if (!cSharpMethodDescriptions.isEmpty()) {
			for (MethodDescription methodDescription : cSharpMethodDescriptions) {
				CompareStatus compareStatus = calculateCompareStatus(methodDescription, isInterface);
				result.addError(compareStatus,
						"No JAVA method found which matches: \n" + methodDescription + "!");
			}
		}
		if (!javaMethodDescriptions.isEmpty()) {
			for (MethodDescription methodDescription : javaMethodDescriptions) {
				CompareStatus compareStatus = calculateCompareStatus(methodDescription, isInterface);
				result.addError(compareStatus,
						"No C# method found which matches: \n" + methodDescription + "!");
			}
		}
	}

	private static CompareStatus calculateCompareStatus(MethodDescription methodDescription,
			boolean isInterface) {
		CompareStatus compareStatus;
		if (isInterface) {
			compareStatus = CompareStatus.INTERFACE_METHOD_NOT_FOUND;
		}
		else if (methodDescription.getModifiers().contains(ParserUtil.MODIFIER_PUBLIC)) {
			compareStatus = CompareStatus.PUBLIC_METHOD_NOT_FOUND;
		}
		else {
			compareStatus = CompareStatus.INTERNAL_METHOD_NOT_FOUND;
		}
		return compareStatus;
	}

	private static String firstToUpperCase(String name) {
		String firstLetter = name.substring(0, 1);
		String firstLetterToUpper = firstLetter.toUpperCase();
		String nameFirstToUpper = name.replaceFirst(firstLetter, firstLetterToUpper);
		return nameFirstToUpper;
	}

	private static String firstToLowerCase(String name) {
		String firstLetter = name.substring(0, 1);
		String firstLetterToLower = firstLetter.toLowerCase();
		String nameFirstToLower = name.replaceFirst(firstLetter, firstLetterToLower);
		return nameFirstToLower;
	}

}
