package de.osthus.classbrowser.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

public class ParserUtilTest
{
	@Test
	public void testAddFieldDescriptions()
	{
		TypeDescription typeDescription = new TypeDescription("null", "null", "null", "null", "null", "null", 0);
		ParserUtil.addFieldDescriptions(TestClass.class, typeDescription);

		List<FieldDescription> fieldDescriptions = typeDescription.getFieldDescriptions();
		assertNotNull(fieldDescriptions);
		assertEquals(4, fieldDescriptions.size());
		assertEquals(1, fieldDescriptions.get(0).getAnnotations().size());
		assertEquals(1, fieldDescriptions.get(1).getAnnotations().size());
	}

	@Test
	public void testAddMethodDescriptions()
	{
		TypeDescription typeDescription = new TypeDescription("null", "null", "null", "null", "null", "null", 0);
		ParserUtil.addMethodDescriptions(TestClass.class, typeDescription);

		List<MethodDescription> methodDescriptions = typeDescription.getMethodDescriptions();
		assertNotNull(methodDescriptions);
		assertEquals(1, methodDescriptions.size());
		assertEquals(1, methodDescriptions.get(0).getAnnotations().size());
	}

	@Test
	public void testGetAnnotationInfo()
	{
		TypeDescription typeDescription = new TypeDescription("null", "null", "null", "null", "null", "null", 0);
		ParserUtil.addFieldDescriptions(TestClass.class, typeDescription);

		List<FieldDescription> fieldDescriptions = typeDescription.getFieldDescriptions();
		assertNotNull(fieldDescriptions);
		assertEquals(4, fieldDescriptions.size());

		FieldDescription logFieldDescription = fieldDescriptions.get(0);
		List<AnnotationInfo> annotations = logFieldDescription.getAnnotations();
		assertEquals(1, annotations.size());
		AnnotationInfo annotationInfo = annotations.get(0);
		assertEquals("de.osthus.ambeth.log.LogInstance", annotationInfo.getType());
		assertEquals(0, annotationInfo.getParameters().size());

		FieldDescription serviceFieldDescription = fieldDescriptions.get(1);
		annotations = serviceFieldDescription.getAnnotations();
		assertEquals(1, annotations.size());
		annotationInfo = annotations.get(0);
		assertEquals("de.osthus.ambeth.ioc.annotation.Autowired", annotationInfo.getType());
		assertEquals(3, annotationInfo.getParameters().size());

		FieldDescription service2FieldDescription = fieldDescriptions.get(2);
		annotations = service2FieldDescription.getAnnotations();
		assertEquals(1, annotations.size());
		annotationInfo = annotations.get(0);
		assertEquals("de.osthus.ambeth.ioc.annotation.Autowired", annotationInfo.getType());
		assertEquals(3, annotationInfo.getParameters().size());

		FieldDescription service3FieldDescription = fieldDescriptions.get(3);
		annotations = service3FieldDescription.getAnnotations();
		assertEquals(1, annotations.size());
		annotationInfo = annotations.get(0);
		assertEquals("de.osthus.ambeth.ioc.annotation.Autowired", annotationInfo.getType());
		assertEquals(3, annotationInfo.getParameters().size());
	}
}
