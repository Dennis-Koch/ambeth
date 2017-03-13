package com.koch.ambeth.log;

import java.lang.reflect.Method;

public class LogTypesUtil
{
	public static String printMethod(Method method, boolean printShortStringNames)
	{
		StringBuilder sb = new StringBuilder();
		printMethod(method, printShortStringNames, sb);
		return sb.toString();
	}

	public static void printMethod(Method method, boolean printShortStringNames, StringBuilder sb)
	{
		if (printShortStringNames)
		{
			printType(method.getReturnType(), printShortStringNames, sb);
			sb.append(' ').append(method.getName());
			sb.append('(');
			printParameters(method.getParameterTypes(), printShortStringNames, sb);
			sb.append(')');
			return;
		}
		sb.append(method.toString());
	}

	public static String printMethod(String methodName, Class<?>[] parameters, Class<?> returnType, boolean printShortStringNames)
	{
		StringBuilder sb = new StringBuilder();
		printMethod(methodName, parameters, returnType, printShortStringNames, sb);
		return sb.toString();
	}

	public static void printMethod(String methodName, Class<?>[] parameters, Class<?> returnType, boolean printShortStringNames, StringBuilder sb)
	{
		if (returnType != null)
		{
			printType(returnType, printShortStringNames, sb);
			sb.append(' ');
		}
		sb.append(methodName);
		sb.append('(');
		printParameters(parameters, printShortStringNames, sb);
		sb.append(')');
	}

	public static void printParameters(Class<?>[] parameters, boolean printShortStringNames, StringBuilder sb)
	{
		if (parameters != null)
		{
			for (int a = 0, size = parameters.length; a < size; a++)
			{
				if (a > 0)
				{
					sb.append(',');
				}
				printType(parameters[a], printShortStringNames, sb);
			}
		}
	}

	public static String printType(Class<?> type, boolean printShortStringNames)
	{
		StringBuilder sb = new StringBuilder();
		printType(type, printShortStringNames, sb);
		return sb.toString();
	}

	public static void printType(Class<?> type, boolean printShortStringNames, StringBuilder sb)
	{
		if (!printShortStringNames)
		{
			sb.append(type.getName());
			return;
		}
		sb.append(type.getSimpleName());
		// Class<?>[] genericArguments = type..GetGenericArguments();
		// if (genericArguments != null && genericArguments.Length > 0)
		// {
		// sb.Append('<');
		// for (int a = 0, size = genericArguments.Length; a < size; a++)
		// {
		// if (a > 0)
		// {
		// sb.Append(',');
		// }
		// PrintType(genericArguments[a], printShortStringNames, sb);
		// }
		// sb.Append('>');
		// }
	}
}
