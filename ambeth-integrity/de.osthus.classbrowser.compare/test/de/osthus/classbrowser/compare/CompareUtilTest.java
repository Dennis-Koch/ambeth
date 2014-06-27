package de.osthus.classbrowser.compare;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import de.osthus.classbrowser.java.FieldDescription;
import de.osthus.classbrowser.java.MethodDescription;
import de.osthus.classbrowser.java.ParserUtil;
import de.osthus.classbrowser.java.TypeDescription;

public class CompareUtilTest
{

	@Test
	public void testIsMethodNameEqual()
	{
		List<String> emptyList = Collections.<String> emptyList();
		CompareResult result = new CompareResult("testIsMethodNameEqual");
		List<CompareError> errors = result.getErrors();
		MethodDescription csharpMethodDescription = new MethodDescription("Validate", "void", emptyList, emptyList);
		MethodDescription javaMethodDescription = new MethodDescription("validate", "void", emptyList, emptyList);
		assertTrue(CompareUtil.isMethodNameEqual(result, csharpMethodDescription, javaMethodDescription));
		assertTrue(errors.isEmpty());

		csharpMethodDescription = new MethodDescription("get_Name", "String", emptyList, emptyList);
		javaMethodDescription = new MethodDescription("getName", "java.lang.String", emptyList, emptyList);
		assertTrue(CompareUtil.isMethodNameEqual(result, csharpMethodDescription, javaMethodDescription));
		assertTrue(errors.isEmpty());

		csharpMethodDescription = new MethodDescription("get_Valid", "bool", emptyList, emptyList);
		javaMethodDescription = new MethodDescription("isValid", "boolean", emptyList, emptyList);
		assertTrue(CompareUtil.isMethodNameEqual(result, csharpMethodDescription, javaMethodDescription));
		assertTrue(errors.isEmpty());

		csharpMethodDescription = new MethodDescription("get_IsValid", "bool", emptyList, emptyList);
		javaMethodDescription = new MethodDescription("isValid", "boolean", emptyList, emptyList);
		assertTrue(CompareUtil.isMethodNameEqual(result, csharpMethodDescription, javaMethodDescription));
		assertTrue(errors.isEmpty());

		csharpMethodDescription = new MethodDescription("get_HasChanges", "bool", emptyList, emptyList);
		javaMethodDescription = new MethodDescription("hasChanges", "boolean", emptyList, emptyList);
		assertTrue(CompareUtil.isMethodNameEqual(result, csharpMethodDescription, javaMethodDescription));
		assertTrue(errors.isEmpty());

		csharpMethodDescription = new MethodDescription("set_IsValid", "void", emptyList, Arrays.asList("bool"));
		javaMethodDescription = new MethodDescription("setValid", "void", emptyList, Arrays.asList("boolean"));
		assertTrue(CompareUtil.isMethodNameEqual(result, csharpMethodDescription, javaMethodDescription));
		assertTrue(errors.isEmpty());

		csharpMethodDescription = new MethodDescription("GetHashCode", "int", emptyList, emptyList);
		javaMethodDescription = new MethodDescription("hashCode", "int", emptyList, emptyList);
		assertTrue(CompareUtil.isMethodNameEqual(result, csharpMethodDescription, javaMethodDescription));
		assertTrue(errors.isEmpty());

		csharpMethodDescription = new MethodDescription("getValue", "int", emptyList, emptyList);
		javaMethodDescription = new MethodDescription("getValue", "int", emptyList, emptyList);
		assertTrue(CompareUtil.isMethodNameEqual(result, csharpMethodDescription, javaMethodDescription));
		assertEquals(1, errors.size());
		errors.clear();

		csharpMethodDescription = new MethodDescription("GetValue", "int", emptyList, emptyList);
		javaMethodDescription = new MethodDescription("GetValue", "int", emptyList, emptyList);
		assertTrue(CompareUtil.isMethodNameEqual(result, csharpMethodDescription, javaMethodDescription));
		assertEquals(1, errors.size());
		errors.clear();
	}

	@Test
	public void testAreMethodModifiersEquivalent()
	{
		List<String> csharpMethodModifiers = Arrays.asList("public", "virtual", "abstract");
		List<String> javaMethodModifiers = Arrays.asList("public", "abstract");
		assertTrue(CompareUtil.areMethodModifiersEquivalent(csharpMethodModifiers, javaMethodModifiers));
	}

	@Test
	public void testIsTypeMatch()
	{
		assertTrue(CompareUtil.isTypeMatch("object", "java.lang.Object", 0));
		assertTrue(CompareUtil.isTypeMatch("Object", "java.lang.Object", 10));
		assertTrue(CompareUtil.isTypeMatch("bool", "boolean", 10));
		assertTrue(CompareUtil.isTypeMatch("string", "java.lang.String", 0));
		assertTrue(CompareUtil.isTypeMatch("String", "java.lang.String", 0));
		assertTrue(CompareUtil.isTypeMatch("System.Collections.Generic.IDictionary", "java.util.Map", 0));
		assertTrue(CompareUtil.isTypeMatch("System.Type", "java.lang.Class", 0));
		assertTrue(CompareUtil.isTypeMatch("System.Xml.Linq.XDocument", "org.w3c.dom.Document", 0));
		assertTrue(CompareUtil.isTypeMatch("System.Xml.Linq.XName", "java.lang.String", 0));
	}

	@Test
	public void testIsTypeMatch_Arrays()
	{
		assertTrue(CompareUtil.isTypeMatch("object[]", "java.lang.Object[]", 0));
		assertTrue(CompareUtil.isTypeMatch("System.Collections.Generic.IDictionary", "java.util.Map", 0));
		assertTrue(CompareUtil.isTypeMatch("System.Xml.Linq.XDocument[]", "org.w3c.dom.Document[]", 0));

		assertTrue(CompareUtil.isTypeMatch("object[][]", "java.lang.Object[][]", 0));
		assertTrue(CompareUtil.isTypeMatch("object[][][][]", "java.lang.Object[][][][]", 0));

		assertFalse(CompareUtil.isTypeMatch("object[][]", "java.lang.Object[]", 0));
	}

	@Test
	public void testIsTypeMatch_Generics()
	{
		assertTrue(CompareUtil.isTypeMatch("System.Collections.Generic.IList<Object>", "java.util.List<java.lang.Object>", 0));

		assertTrue(CompareUtil.isTypeMatch("De.Osthus.Ambeth.Merge.Model.IObjRef", "de.osthus.ambeth.merge.model.IObjRef", 0));
		assertTrue(CompareUtil.isTypeMatch("System.Collections.Generic.IList<De.Osthus.Ambeth.Merge.Model.IObjRef>",
				"java.util.List<de.osthus.ambeth.merge.model.IObjRef>", 0));

		assertTrue(CompareUtil.isTypeMatch("System.Collections.Generic.IDictionary<string, System.Collections.Generic.IList<System.Xml.Linq.XElement>>",
				"de.osthus.ambeth.collections.IMap<java.lang.String, de.osthus.ambeth.collections.IList<org.w3c.dom.Element>>", 0));

		assertFalse(CompareUtil
				.isTypeMatch("System.Collections.Generic.IList<De.Osthus.Ambeth.Merge.Model.IObjRef>", "de.osthus.ambeth.merge.model.IObjRef", 0));
		assertFalse(CompareUtil.isTypeMatch("De.Osthus.Ambeth.Merge.Model.IObjRef", "java.util.List<de.osthus.ambeth.merge.model.IObjRef>", 0));
	}

	@Ignore
	// Not yet imlemented
	@Test
	public void testIsTypeMatch_GenericsContent()
	{
		assertTrue(CompareUtil.isTypeMatch("System.Collections.Generic.IList<Object>", "java.util.List<java.lang.Object>", 0));
		assertTrue(CompareUtil.isTypeMatch("System.Collections.Generic.IList<String>", "java.util.List<java.lang.String>", 0));

		assertFalse(CompareUtil.isTypeMatch("System.Collections.Generic.IList<Object>", "java.util.List<java.lang.Integer>", 0));
		assertFalse(CompareUtil.isTypeMatch("System.Collections.Generic.IList<String>", "java.util.List<java.lang.Object>", 0));
	}

	@Ignore
	// Not yet imlemented
	@Test
	public void testIsTypeMatch_MultipleGenerics()
	{
		assertTrue(CompareUtil.isTypeMatch("System.Collections.Generic.IDictionary<Object, Object>", "java.util.Map<java.lang.Object, java.lang.Object>", 0));
		assertTrue(CompareUtil.isTypeMatch("System.Collections.Generic.IDictionary<String, Object>", "java.util.Map<java.lang.String, java.lang.Object>", 0));

		assertFalse(CompareUtil.isTypeMatch("System.Collections.Generic.IDictionary<Object, Object>", "java.util.Map<java.lang.Object, java.lang.Integer>", 0));
		assertFalse(CompareUtil.isTypeMatch("System.Collections.Generic.IDictionary<String, Object>", "java.util.Map<java.lang.Object, java.lang.String>", 0));
	}

	@Ignore
	// Not yet imlemented
	@Test
	public void testIsTypeMatch_MultiLevelGenerics()
	{
		assertTrue(CompareUtil.isTypeMatch("System.Collections.Generic.IList<System.Collections.Generic.IList<Object>>",
				"java.util.List<java.util.List<java.lang.Object>>", 0));
		assertTrue(CompareUtil.isTypeMatch("System.Collections.Generic.IList<System.Collections.Generic.IList<String>>",
				"java.util.List<java.util.List<java.lang.String>>", 0));

		assertFalse(CompareUtil.isTypeMatch("System.Collections.Generic.IList<Object>", "java.util.List<java.util.List<java.lang.Object>>", 0));
		assertFalse(CompareUtil.isTypeMatch("System.Collections.Generic.IList<System.Collections.Generic.IList<String>>",
				"java.util.List<java.util.Set<java.lang.String>>", 0));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIsTypeMatch_maxRecursionReached()
	{
		CompareUtil.isTypeMatch("", "", 11);
	}

	@Test
	public void testIsMethodEqual()
	{
		CompareResult result = new CompareResult("testIsMethodEqual");
		MethodDescription cSharpMethodDescription = new MethodDescription("Validate", "void", Arrays.asList("public", "virtual", "abstract"),
				Arrays.asList("System.Xml.Linq.XDocument"));
		MethodDescription javaMethodDescription = new MethodDescription("validate", "void", Arrays.asList("public", "abstract"),
				Arrays.asList("org.w3c.dom.Document"));
		assertTrue(CompareUtil.isMethodEqual(result, cSharpMethodDescription, javaMethodDescription));
	}

	@Test
	public void testCheckForAnnotationValueMethod()
	{
		MethodDescription cSharpGetter = new MethodDescription("get_Name", "String", Arrays.asList("public"), Collections.<String> emptyList());
		MethodDescription cSharpSetter = new MethodDescription("set_Name", "void", Arrays.asList("private"), Arrays.asList("String"));
		MethodDescription javaGetter = new MethodDescription("value", "java.lang.String", Arrays.asList("public", "abstract"), Collections.<String> emptyList());
		List<MethodDescription> cSharpMethods = new ArrayList<MethodDescription>(Arrays.asList(cSharpGetter, cSharpSetter));
		List<MethodDescription> javaMethods = new ArrayList<MethodDescription>(Arrays.asList(javaGetter));
		CompareUtil.checkForAnnotationValueMethod(cSharpMethods, javaMethods);
		assertTrue(cSharpMethods.isEmpty());
		assertTrue(javaMethods.isEmpty());
	}

	@Test
	public void testCheckForAnnotationValueMethod_valueArray()
	{
		MethodDescription cSharpGetter = new MethodDescription("get_Name", "String", Arrays.asList("public"), Collections.<String> emptyList());
		MethodDescription cSharpSetter = new MethodDescription("set_Name", "void", Arrays.asList("private"), Arrays.asList("String"));
		MethodDescription javaGetter = new MethodDescription("value", "java.lang.String[]", Arrays.asList("public", "abstract"),
				Collections.<String> emptyList());
		List<MethodDescription> cSharpMethods = new ArrayList<MethodDescription>(Arrays.asList(cSharpGetter, cSharpSetter));
		List<MethodDescription> javaMethods = new ArrayList<MethodDescription>(Arrays.asList(javaGetter));
		CompareUtil.checkForAnnotationValueMethod(cSharpMethods, javaMethods);
		assertTrue(cSharpMethods.isEmpty());
		assertTrue(javaMethods.isEmpty());
	}

	@Test
	public void testCheckTypeAndGenericParams_annotation()
	{
		CompareResult result = new CompareResult("de.osthus.ambeth.annotation.Testing");
		TypeDescription javaType = new TypeDescription("java source", "Integrity.Test", "de.osthus.ambeth.annotation", "Testing",
				"de.osthus.ambeth.annotation.Testing", "annotation", 0);
		TypeDescription csharpType = new TypeDescription("C# source", "Integrity.Test", "De.Osthus.Ambeth.Annotation", "Testing",
				"de.osthus.ambeth.annotation.Testing", "annotation", 0);
		String expectedTypeClass = ParserUtil.TYPE_ANNOTATION;
		String infoPrefix = "Test '";

		assertTrue(CompareUtil.checkTypeAndGenericParams(result, javaType, csharpType, expectedTypeClass, infoPrefix));
	}

	@Test
	public void testHandleLoggerProperty()
	{
		CompareResult result = new CompareResult("de.osthus.ambeth.annotation.Testing");
		MethodDescription validGetter = new MethodDescription(CompareUtil.LOG_PROPERTY_GETTER_NAME, CompareUtil.LOG_TYPE_CSHARP,
				Arrays.asList(ParserUtil.MODIFIER_PRIVATE), Collections.<String> emptyList());
		MethodDescription invalidGetter = new MethodDescription(CompareUtil.LOG_PROPERTY_GETTER_NAME, CompareUtil.LOG_TYPE_CSHARP,
				Arrays.asList(ParserUtil.MODIFIER_PROTECTED), Collections.<String> emptyList());
		MethodDescription validSetter = new MethodDescription(CompareUtil.LOG_PROPERTY_SETTER_NAME, CompareUtil.TYPE_VOID,
				Arrays.asList(ParserUtil.MODIFIER_PUBLIC), Arrays.asList(CompareUtil.LOG_TYPE_CSHARP));
		FieldDescription validField = new FieldDescription(CompareUtil.LOG_PROPERTY_FIELD_NAME, CompareUtil.LOG_TYPE_JAVA,
				Arrays.asList(ParserUtil.MODIFIER_PRIVATE));

		LinkedHashMap<String, List<MethodDescription>> nameToCSharpMethodDescriptionMap = createMethodDescriptionMap(validGetter, validSetter);
		HashMap<String, FieldDescription> nameToJavaFieldDescriptionMap = createFieldDescriptionMap(validField);

		CompareUtil.handleLoggerProperty(result, nameToCSharpMethodDescriptionMap, nameToJavaFieldDescriptionMap);
		assertTrue(result.getErrors().isEmpty());
		assertTrue(nameToCSharpMethodDescriptionMap.isEmpty());
		assertTrue(nameToJavaFieldDescriptionMap.isEmpty());

		nameToCSharpMethodDescriptionMap = createMethodDescriptionMap(invalidGetter, validSetter);
		nameToJavaFieldDescriptionMap = createFieldDescriptionMap(validField);
		CompareUtil.handleLoggerProperty(result, nameToCSharpMethodDescriptionMap, nameToJavaFieldDescriptionMap);
		assertEquals(1, result.getErrors().size());
		assertTrue(nameToCSharpMethodDescriptionMap.isEmpty());
		assertTrue(nameToJavaFieldDescriptionMap.isEmpty());
	}

	@Test
	public void testHandleInjectionPoints_default()
	{
		CompareResult result = new CompareResult("de.osthus.ambeth.annotation.Testing");
		String propertyName = "Name";
		MethodDescription validProtectedCSharpGetter = new MethodDescription("get_" + propertyName, "Object", Arrays.asList(ParserUtil.MODIFIER_PROTECTED,
				ParserUtil.MODIFIER_VIRTUAL), Collections.<String> emptyList());
		MethodDescription validCSharpSetter = new MethodDescription("set_" + propertyName, CompareUtil.TYPE_VOID, Arrays.asList(ParserUtil.MODIFIER_PUBLIC,
				ParserUtil.MODIFIER_VIRTUAL), Arrays.asList("Object"));
		MethodDescription validJavaSetter = new MethodDescription("set" + propertyName, CompareUtil.TYPE_VOID, Arrays.asList(ParserUtil.MODIFIER_PUBLIC),
				Arrays.asList("java.lang.Object"));

		// Default case
		LinkedHashMap<String, List<MethodDescription>> nameToCSharpMethodDescriptionMap = createMethodDescriptionMap(validProtectedCSharpGetter,
				validCSharpSetter);
		LinkedHashMap<String, List<MethodDescription>> nameToJavaMethodDescriptionMap = createMethodDescriptionMap(validJavaSetter);

		CompareUtil.handleInjectionPoints(result, nameToCSharpMethodDescriptionMap, nameToJavaMethodDescriptionMap,
				Collections.<String, FieldDescription> emptyMap());
		assertTrue(result.getErrors().isEmpty());
		assertTrue(nameToCSharpMethodDescriptionMap.isEmpty());
		assertTrue(nameToJavaMethodDescriptionMap.isEmpty());
	}

	@Test
	public void testHandleInjectionPoints_defaultNotVirtual()
	{
		CompareResult result = new CompareResult("de.osthus.ambeth.annotation.Testing");
		String propertyName = "Name";
		MethodDescription validProtectedCSharpGetter = new MethodDescription("get_" + propertyName, "Object", Arrays.asList(ParserUtil.MODIFIER_PROTECTED),
				Collections.<String> emptyList());
		MethodDescription validCSharpSetter = new MethodDescription("set_" + propertyName, CompareUtil.TYPE_VOID, Arrays.asList(ParserUtil.MODIFIER_PUBLIC),
				Arrays.asList("Object"));
		MethodDescription validJavaSetter = new MethodDescription("set" + propertyName, CompareUtil.TYPE_VOID, Arrays.asList(ParserUtil.MODIFIER_PUBLIC),
				Arrays.asList("java.lang.Object"));

		// Default case
		LinkedHashMap<String, List<MethodDescription>> nameToCSharpMethodDescriptionMap = createMethodDescriptionMap(validProtectedCSharpGetter,
				validCSharpSetter);
		LinkedHashMap<String, List<MethodDescription>> nameToJavaMethodDescriptionMap = createMethodDescriptionMap(validJavaSetter);

		CompareUtil.handleInjectionPoints(result, nameToCSharpMethodDescriptionMap, nameToJavaMethodDescriptionMap,
				Collections.<String, FieldDescription> emptyMap());
		assertTrue(result.getErrors().isEmpty());
		assertTrue(nameToCSharpMethodDescriptionMap.isEmpty());
		assertTrue(nameToJavaMethodDescriptionMap.isEmpty());
	}

	@Test
	public void testHandleInjectionPoints_publicJavaGetter()
	{
		CompareResult result = new CompareResult("de.osthus.ambeth.annotation.Testing");
		String propertyName = "Name";
		MethodDescription validPublicCSharpGetter = new MethodDescription("get_" + propertyName, "Object", Arrays.asList(ParserUtil.MODIFIER_PUBLIC,
				ParserUtil.MODIFIER_VIRTUAL), Collections.<String> emptyList());
		MethodDescription validCSharpSetter = new MethodDescription("set_" + propertyName, CompareUtil.TYPE_VOID, Arrays.asList(ParserUtil.MODIFIER_PUBLIC,
				ParserUtil.MODIFIER_VIRTUAL), Arrays.asList("Object"));
		MethodDescription validPublicJavaGetter = new MethodDescription("get" + propertyName, "java.lang.Object", Arrays.asList(ParserUtil.MODIFIER_PUBLIC),
				Collections.<String> emptyList());
		MethodDescription validJavaSetter = new MethodDescription("set" + propertyName, CompareUtil.TYPE_VOID, Arrays.asList(ParserUtil.MODIFIER_PUBLIC),
				Arrays.asList("java.lang.Object"));

		// With optional public Java getter
		LinkedHashMap<String, List<MethodDescription>> nameToCSharpMethodDescriptionMap = createMethodDescriptionMap(validPublicCSharpGetter, validCSharpSetter);
		LinkedHashMap<String, List<MethodDescription>> nameToJavaMethodDescriptionMap = createMethodDescriptionMap(validPublicJavaGetter, validJavaSetter);

		CompareUtil.handleInjectionPoints(result, nameToCSharpMethodDescriptionMap, nameToJavaMethodDescriptionMap,
				Collections.<String, FieldDescription> emptyMap());
		assertTrue(result.getErrors().isEmpty());
		assertTrue(nameToCSharpMethodDescriptionMap.isEmpty());
		assertTrue(nameToJavaMethodDescriptionMap.isEmpty());
	}

	@Test
	public void testHandleInjectionPoints_protectedJavaGetter()
	{
		CompareResult result = new CompareResult("de.osthus.ambeth.annotation.Testing");
		String propertyName = "Name";
		MethodDescription validProtectedCSharpGetter = new MethodDescription("get_" + propertyName, "Object", Arrays.asList(ParserUtil.MODIFIER_PROTECTED,
				ParserUtil.MODIFIER_VIRTUAL), Collections.<String> emptyList());
		MethodDescription validCSharpSetter = new MethodDescription("set_" + propertyName, CompareUtil.TYPE_VOID, Arrays.asList(ParserUtil.MODIFIER_PUBLIC,
				ParserUtil.MODIFIER_VIRTUAL), Arrays.asList("Object"));
		MethodDescription validProtectedJavaGetter = new MethodDescription("get" + propertyName, "java.lang.Object",
				Arrays.asList(ParserUtil.MODIFIER_PROTECTED), Collections.<String> emptyList());
		MethodDescription validJavaSetter = new MethodDescription("set" + propertyName, CompareUtil.TYPE_VOID, Arrays.asList(ParserUtil.MODIFIER_PUBLIC),
				Arrays.asList("java.lang.Object"));

		// With optional protected Java getter
		Map<String, List<MethodDescription>> nameToCSharpMethodDescriptionMap = createMethodDescriptionMap(validProtectedCSharpGetter, validCSharpSetter);
		Map<String, List<MethodDescription>> nameToJavaMethodDescriptionMap = createMethodDescriptionMap(validProtectedJavaGetter, validJavaSetter);

		CompareUtil.handleInjectionPoints(result, nameToCSharpMethodDescriptionMap, nameToJavaMethodDescriptionMap,
				Collections.<String, FieldDescription> emptyMap());
		assertTrue(result.getErrors().isEmpty());
		assertTrue(nameToCSharpMethodDescriptionMap.isEmpty());
		assertTrue(nameToJavaMethodDescriptionMap.isEmpty());
	}

	@Test
	public void testHandleInjectionPoints_javaGetterMismatchingCSharp()
	{
		CompareResult result = new CompareResult("de.osthus.ambeth.annotation.Testing");
		String propertyName = "Name";
		MethodDescription validProtectedCSharpGetter = new MethodDescription("get_" + propertyName, "Object", Arrays.asList(ParserUtil.MODIFIER_PROTECTED,
				ParserUtil.MODIFIER_VIRTUAL), Collections.<String> emptyList());
		MethodDescription validCSharpSetter = new MethodDescription("set_" + propertyName, CompareUtil.TYPE_VOID, Arrays.asList(ParserUtil.MODIFIER_PUBLIC,
				ParserUtil.MODIFIER_VIRTUAL), Arrays.asList("Object"));
		MethodDescription validPublicJavaGetter = new MethodDescription("get" + propertyName, "java.lang.Object", Arrays.asList(ParserUtil.MODIFIER_PUBLIC),
				Collections.<String> emptyList());
		MethodDescription validJavaSetter = new MethodDescription("set" + propertyName, CompareUtil.TYPE_VOID, Arrays.asList(ParserUtil.MODIFIER_PUBLIC),
				Arrays.asList("java.lang.Object"));

		// With optional public Java getter, but protected C# getter
		LinkedHashMap<String, List<MethodDescription>> nameToCSharpMethodDescriptionMap = createMethodDescriptionMap(validProtectedCSharpGetter,
				validCSharpSetter);
		LinkedHashMap<String, List<MethodDescription>> nameToJavaMethodDescriptionMap = createMethodDescriptionMap(validPublicJavaGetter, validJavaSetter);

		CompareUtil.handleInjectionPoints(result, nameToCSharpMethodDescriptionMap, nameToJavaMethodDescriptionMap,
				Collections.<String, FieldDescription> emptyMap());
		List<CompareError> errors = result.getErrors();
		assertEquals(1, errors.size());
		assertEquals(CompareStatus.PATTERN_VIOLATION, errors.get(0).getStatus());
		assertTrue(nameToCSharpMethodDescriptionMap.isEmpty());
		assertTrue(nameToJavaMethodDescriptionMap.isEmpty());
	}

	@Test
	public void testHandleInjectionPoints_javaAutowired()
	{
		CompareResult result = new CompareResult("de.osthus.ambeth.annotation.Testing");
		String propertyName = "Name";
		MethodDescription validCSharpGetter = new MethodDescription("get_" + propertyName, "Object", Arrays.asList(ParserUtil.MODIFIER_PROTECTED,
				ParserUtil.MODIFIER_VIRTUAL), Collections.<String> emptyList());
		MethodDescription validCSharpSetter = new MethodDescription("set_" + propertyName, CompareUtil.TYPE_VOID, Arrays.asList(ParserUtil.MODIFIER_PUBLIC,
				ParserUtil.MODIFIER_VIRTUAL), Arrays.asList("Object"));
		FieldDescription validField = new FieldDescription("name", "java.lang.Object", Arrays.asList(ParserUtil.MODIFIER_PROTECTED));
		FieldDescription invalidField = new FieldDescription("name", "java.lang.Object", Arrays.asList(ParserUtil.MODIFIER_PRIVATE));

		// No Java setter but annotated field
		Map<String, List<MethodDescription>> nameToCSharpMethodDescriptionMap = createMethodDescriptionMap(validCSharpGetter, validCSharpSetter);
		Map<String, FieldDescription> nameToJavaFieldDescriptionMap = createFieldDescriptionMap(validField);

		CompareUtil.handleInjectionPoints(result, nameToCSharpMethodDescriptionMap, Collections.<String, List<MethodDescription>> emptyMap(),
				nameToJavaFieldDescriptionMap);
		assertTrue(result.getErrors().isEmpty());
		assertTrue(nameToCSharpMethodDescriptionMap.isEmpty());
		assertTrue(nameToJavaFieldDescriptionMap.isEmpty());

		// Invalid field, should not be protected
		nameToCSharpMethodDescriptionMap = createMethodDescriptionMap(validCSharpGetter, validCSharpSetter);
		nameToJavaFieldDescriptionMap = createFieldDescriptionMap(invalidField);

		CompareUtil.handleInjectionPoints(result, nameToCSharpMethodDescriptionMap, Collections.<String, List<MethodDescription>> emptyMap(),
				nameToJavaFieldDescriptionMap);
		List<CompareError> errors = result.getErrors();
		assertEquals(1, errors.size());
		assertEquals(CompareStatus.PATTERN_VIOLATION, errors.get(0).getStatus());
		assertTrue(nameToCSharpMethodDescriptionMap.isEmpty());
		assertTrue(nameToJavaFieldDescriptionMap.isEmpty());
	}

	@Test
	public void testHandleInjectionPoints_propertyMethod()
	{
		CompareResult result = new CompareResult("de.osthus.ambeth.annotation.Testing");
		String propertyName = "Name";
		MethodDescription validProtectedCSharpGetter = new MethodDescription("get_" + propertyName, "String", Arrays.asList(ParserUtil.MODIFIER_PROTECTED,
				ParserUtil.MODIFIER_VIRTUAL), Collections.<String> emptyList());
		MethodDescription validCSharpSetter = new MethodDescription("set_" + propertyName, CompareUtil.TYPE_VOID, Arrays.asList(ParserUtil.MODIFIER_PUBLIC,
				ParserUtil.MODIFIER_VIRTUAL), Arrays.asList("String"));
		MethodDescription validJavaSetter = new MethodDescription("set" + propertyName, CompareUtil.TYPE_VOID, Arrays.asList(ParserUtil.MODIFIER_PUBLIC),
				Arrays.asList("java.lang.String"));

		// Un-annotated
		LinkedHashMap<String, List<MethodDescription>> nameToCSharpMethodDescriptionMap = createMethodDescriptionMap(validProtectedCSharpGetter,
				validCSharpSetter);
		LinkedHashMap<String, List<MethodDescription>> nameToJavaMethodDescriptionMap = createMethodDescriptionMap(validJavaSetter);

		CompareUtil.handleInjectionPoints(result, nameToCSharpMethodDescriptionMap, nameToJavaMethodDescriptionMap,
				Collections.<String, FieldDescription> emptyMap());
		assertTrue(result.getErrors().isEmpty());
		assertEquals(2, nameToCSharpMethodDescriptionMap.size());
		assertEquals(1, nameToJavaMethodDescriptionMap.size());

		// Default case
		validJavaSetter.getAnnotations().add(ParserUtil.JAVA_ANNOTATION_PROPERTY);
		CompareUtil.handleInjectionPoints(result, nameToCSharpMethodDescriptionMap, nameToJavaMethodDescriptionMap,
				Collections.<String, FieldDescription> emptyMap());
		assertTrue(result.getErrors().isEmpty());
		assertTrue(nameToCSharpMethodDescriptionMap.isEmpty());
		assertTrue(nameToJavaMethodDescriptionMap.isEmpty());
	}

	protected LinkedHashMap<String, List<MethodDescription>> createMethodDescriptionMap(MethodDescription... methodDescriptions)
	{
		LinkedHashMap<String, List<MethodDescription>> nameToMethodDescriptionMap = new LinkedHashMap<String, List<MethodDescription>>();
		for (MethodDescription methodDescription : methodDescriptions)
		{
			nameToMethodDescriptionMap.put(methodDescription.getName(), Arrays.asList(methodDescription));
		}
		return nameToMethodDescriptionMap;
	}

	protected HashMap<String, FieldDescription> createFieldDescriptionMap(FieldDescription... fieldDescriptions)
	{
		LinkedHashMap<String, FieldDescription> nameToFieldDescriptionMap = new LinkedHashMap<String, FieldDescription>();
		for (FieldDescription fieldDescription : fieldDescriptions)
		{
			nameToFieldDescriptionMap.put(fieldDescription.getName(), fieldDescription);
		}
		return nameToFieldDescriptionMap;
	}

}
