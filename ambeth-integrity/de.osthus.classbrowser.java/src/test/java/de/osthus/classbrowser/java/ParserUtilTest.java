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
		assertEquals(2, fieldDescriptions.size());
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

}
