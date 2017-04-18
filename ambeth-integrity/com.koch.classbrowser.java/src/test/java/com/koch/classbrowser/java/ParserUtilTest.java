package com.koch.classbrowser.java;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class ParserUtilTest {
	@Test
	public void testAnalyzeClasses() {
		List<ClassHolder> classes = Collections
				.singletonList(new ClassHolder("C:\\dev\\lib\\jambeth-ioc-2.2.321.jar", TestClass.class));
		Map<String, String> moduleMap = Collections.emptyMap();
		List<String> modulesToBeAnalyzed = Collections.emptyList();
		List<TypeDescription> typeDescriptions =
				ParserUtil.analyzeClasses(classes, moduleMap, modulesToBeAnalyzed);

		Assert.assertEquals(1, typeDescriptions.size());
		TypeDescription typeDescription = typeDescriptions.get(0);

		Assert.assertEquals("com.koch.classbrowser.java.AbstractTestClass",
				typeDescription.getSuperType());
		List<String> interfaces = typeDescription.getInterfaces();
		Assert.assertEquals(2, interfaces.size());
		Assert.assertTrue(interfaces.contains("com.koch.classbrowser.java.TestInterface1"));
		Assert.assertTrue(interfaces.contains("com.koch.classbrowser.java.TestInterface2"));
	}

	@Test
	public void testAddFieldDescriptions() {
		TypeDescription typeDescription =
				new TypeDescription("null", "null", "null", "null", "null", "null", 0);
		ParserUtil.addFieldDescriptions(TestClass.class, typeDescription);

		List<FieldDescription> fieldDescriptions = typeDescription.getFieldDescriptions();
		Assert.assertNotNull(fieldDescriptions);
		Assert.assertEquals(7, fieldDescriptions.size());
		Assert.assertEquals(0, fieldDescriptions.get(0).getAnnotations().size());
		Assert.assertEquals(0, fieldDescriptions.get(1).getAnnotations().size());
		Assert.assertEquals(1, fieldDescriptions.get(2).getAnnotations().size());
		Assert.assertEquals(1, fieldDescriptions.get(3).getAnnotations().size());

		// Check initialValue of constant fields
		Assert.assertEquals("test constant", fieldDescriptions.get(0).getInitialValue());
		Assert.assertEquals("42", fieldDescriptions.get(1).getInitialValue());
	}

	@Test
	public void testAddMethodDescriptions() {
		TypeDescription typeDescription =
				new TypeDescription("null", "null", "null", "null", "null", "null", 0);
		ParserUtil.addMethodDescriptions(TestClass.class, typeDescription);

		List<MethodDescription> methodDescriptions = typeDescription.getMethodDescriptions();
		Assert.assertNotNull(methodDescriptions);
		Assert.assertEquals(1, methodDescriptions.size());
		Assert.assertEquals(1, methodDescriptions.get(0).getAnnotations().size());
	}

	@Test
	public void testGetAnnotationInfo() {
		TypeDescription typeDescription =
				new TypeDescription("null", "null", "null", "null", "null", "null", 0);
		ParserUtil.addFieldDescriptions(TestClass.class, typeDescription);

		List<FieldDescription> fieldDescriptions = typeDescription.getFieldDescriptions();
		Assert.assertNotNull(fieldDescriptions);
		Assert.assertEquals(7, fieldDescriptions.size());

		// Check FieldDescriptors
		FieldDescription logFieldDescription = fieldDescriptions.get(2);
		List<AnnotationInfo> annotations = logFieldDescription.getAnnotations();
		Assert.assertEquals(1, annotations.size());
		AnnotationInfo annotationInfo = annotations.get(0);
		Assert.assertEquals("com.koch.ambeth.log.LogInstance", annotationInfo.getAnnotationType());
		Assert.assertEquals(0, annotationInfo.getParameters().size());

		FieldDescription serviceFieldDescription = fieldDescriptions.get(3);
		annotations = serviceFieldDescription.getAnnotations();
		Assert.assertEquals(1, annotations.size());
		annotationInfo = annotations.get(0);
		Assert.assertEquals("com.koch.ambeth.ioc.annotation.Autowired",
				annotationInfo.getAnnotationType());
		Assert.assertEquals(3, annotationInfo.getParameters().size());

		FieldDescription service2FieldDescription = fieldDescriptions.get(4);
		annotations = service2FieldDescription.getAnnotations();
		Assert.assertEquals(1, annotations.size());
		annotationInfo = annotations.get(0);
		Assert.assertEquals("com.koch.ambeth.ioc.annotation.Autowired",
				annotationInfo.getAnnotationType());
		Assert.assertEquals(3, annotationInfo.getParameters().size());

		FieldDescription service3FieldDescription = fieldDescriptions.get(5);
		annotations = service3FieldDescription.getAnnotations();
		Assert.assertEquals(1, annotations.size());
		annotationInfo = annotations.get(0);
		Assert.assertEquals("com.koch.ambeth.ioc.annotation.Autowired",
				annotationInfo.getAnnotationType());
		List<AnnotationParamInfo> parameters = annotationInfo.getParameters();
		Assert.assertEquals(3, parameters.size());

		// Check parameters of the @Autowired of the last field
		HashMap<String, Object[]> expected = new HashMap<>();
		expected.put("name", new Object[] {"java.lang.String", "", "test"});
		expected.put("optional", new Object[] {"boolean", false, true});
		expected.put("value", new Object[] {"java.lang.Class", Object.class, Object.class});

		for (int i = 0; i < 3; i++) {
			AnnotationParamInfo annotationParamInfo = parameters.get(i);
			String paramName = annotationParamInfo.getName();
			Assert.assertTrue(expected.containsKey(paramName));
			Object[] expectedValues = expected.get(paramName);
			Assert.assertEquals(expectedValues[0], annotationParamInfo.getType());
			Assert.assertEquals(expectedValues[1], annotationParamInfo.getDefaultValue());
			Assert.assertEquals(expectedValues[2], annotationParamInfo.getCurrentValue());
		}
	}
}
