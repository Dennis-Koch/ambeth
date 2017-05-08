package com.koch.classbrowser.compare;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.koch.classbrowser.compare.CompareResult;
import com.koch.classbrowser.compare.CompareUtil;
import com.koch.classbrowser.java.MethodDescription;

public class FindProblem
{

	private static final Pattern METHOD_DESCRIPTION_TEXT = Pattern
			.compile("MethodDescription\\[method modifiers=\\[(.+)\\],method name=(.+),param types=\\[(.+)\\],return type=(.+)\\]");

	public static void main(String[] args)
	{
		String cSharpMethodText = "MethodDescription[method modifiers=[protected],method name=handlePrimitiveCollections,param types=[De.Osthus.Ambeth.Merge.ValueObjectConfig, System.Collections.Generic.IDictionary<string, System.Collections.Generic.IList<System.Xml.Linq.XElement>>],return type=void]";
		String javaMethodText = "MethodDescription[method modifiers=[protected],method name=handlePrimitiveCollections,param types=[de.osthus.ambeth.merge.ValueObjectConfig, de.osthus.ambeth.collections.IMap<java.lang.String, de.osthus.ambeth.collections.IList<org.w3c.dom.Element>>],return type=void]";

		MethodDescription cSharpMethodDescription = createMethodDescription(cSharpMethodText);
		MethodDescription javaMethodDescription = createMethodDescription(javaMethodText);

		compare(cSharpMethodDescription, javaMethodDescription);
	}

	private static void compare(MethodDescription cSharpMethodDescription, MethodDescription javaMethodDescription)
	{
		CompareResult result = new CompareResult("testIsMethodEqual");
		if (!CompareUtil.isMethodNameEqual(result, cSharpMethodDescription, javaMethodDescription))
		{
			System.out.println("Method names");
		}
		if (!CompareUtil.isTypeMatch(cSharpMethodDescription.getReturnType(), javaMethodDescription.getReturnType()))
		{
			System.out.println("Return types");
		}
		if (!CompareUtil.areMethodParameterTypesEquivalent(cSharpMethodDescription.getParameterTypes(), javaMethodDescription.getParameterTypes()))
		{
			System.out.println("Parameters");
		}
		if (!CompareUtil.areMethodModifiersEquivalent(cSharpMethodDescription.getModifiers(), javaMethodDescription.getModifiers()))
		{
			System.out.println("Modifiers");
		}
	}

	private static MethodDescription createMethodDescription(String methodText)
	{
		Matcher matcher = METHOD_DESCRIPTION_TEXT.matcher(methodText);
		if (!matcher.matches())
		{
			throw new IllegalArgumentException("Description does not match: " + methodText);
		}

		String modifierString = matcher.group(1);
		String methodName = matcher.group(2);
		String parameterString = matcher.group(3);
		String returnType = matcher.group(4);

		String[] modifierArray = modifierString.split(", ");
		List<String> modifiers = Arrays.asList(modifierArray);

		String[] parameterArray = parameterString.split(", ");
		List<String> parameters = Arrays.asList(parameterArray);

		MethodDescription methodDescription = new MethodDescription(methodName, returnType, modifiers, parameters);

		return methodDescription;
	}

}
