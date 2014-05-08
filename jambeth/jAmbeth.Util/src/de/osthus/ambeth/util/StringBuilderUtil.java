package de.osthus.ambeth.util;

import de.osthus.ambeth.objectcollector.IObjectCollector;

public final class StringBuilderUtil
{
	private StringBuilderUtil()
	{
	}

	public static String concat(IObjectCollector objectCollector, Object s1, Object s2)
	{
		StringBuilder sb = objectCollector.create(StringBuilder.class);
		try
		{
			sb.append(s1).append(s2);
			return sb.toString();
		}
		finally
		{
			objectCollector.dispose(sb);
		}
	}

	public static String concat(IObjectCollector objectCollector, Object s1, Object s2, Object s3)
	{
		StringBuilder sb = objectCollector.create(StringBuilder.class);
		try
		{
			sb.append(s1).append(s2).append(s3);
			return sb.toString();
		}
		finally
		{
			objectCollector.dispose(sb);
		}
	}

	public static String concat(IObjectCollector objectCollector, Object s1, Object s2, Object s3, Object s4)
	{
		StringBuilder sb = objectCollector.create(StringBuilder.class);
		try
		{
			sb.append(s1).append(s2).append(s3).append(s4);
			return sb.toString();
		}
		finally
		{
			objectCollector.dispose(sb);
		}
	}

	public static String concat(IObjectCollector objectCollector, Object s1, Object s2, Object s3, Object s4, Object s5)
	{
		StringBuilder sb = objectCollector.create(StringBuilder.class);
		try
		{
			sb.append(s1).append(s2).append(s3).append(s4).append(s5);
			return sb.toString();
		}
		finally
		{
			objectCollector.dispose(sb);
		}
	}

	public static String concat(IObjectCollector objectCollector, Object s1, Object s2, Object s3, Object s4, Object s5, Object s6)
	{
		StringBuilder sb = objectCollector.create(StringBuilder.class);
		try
		{
			sb.append(s1).append(s2).append(s3).append(s4).append(s5).append(s6);
			return sb.toString();
		}
		finally
		{
			objectCollector.dispose(sb);
		}
	}

	public static String concat(IObjectCollector objectCollector, Object s1, Object s2, Object s3, Object s4, Object s5, Object s6, Object s7)
	{
		StringBuilder sb = objectCollector.create(StringBuilder.class);
		try
		{
			sb.append(s1).append(s2).append(s3).append(s4).append(s5).append(s6).append(s7);
			return sb.toString();
		}
		finally
		{
			objectCollector.dispose(sb);
		}
	}

	public static String concat(IObjectCollector objectCollector, Object s1, Object s2, Object s3, Object s4, Object s5, Object s6, Object s7, Object s8)
	{
		StringBuilder sb = objectCollector.create(StringBuilder.class);
		try
		{
			sb.append(s1).append(s2).append(s3).append(s4).append(s5).append(s6).append(s7).append(s8);
			return sb.toString();
		}
		finally
		{
			objectCollector.dispose(sb);
		}
	}

	public static String concat(IObjectCollector objectCollector, Object s1, Object s2, Object s3, Object s4, Object s5, Object s6, Object s7, Object s8,
			String s9)
	{
		StringBuilder sb = objectCollector.create(StringBuilder.class);
		try
		{
			sb.append(s1).append(s2).append(s3).append(s4).append(s5).append(s6).append(s7).append(s8);
			sb.append(s9);
			return sb.toString();
		}
		finally
		{
			objectCollector.dispose(sb);
		}
	}

	public static String concat(IObjectCollector objectCollector, Object s1, Object s2, Object s3, Object s4, Object s5, Object s6, Object s7, Object s8,
			String s9, Object s10)
	{
		StringBuilder sb = objectCollector.create(StringBuilder.class);
		try
		{
			sb.append(s1).append(s2).append(s3).append(s4).append(s5).append(s6).append(s7).append(s8);
			sb.append(s9).append(s10);
			return sb.toString();
		}
		finally
		{
			objectCollector.dispose(sb);
		}
	}

	public static String concat(IObjectCollector objectCollector, Object s1, Object s2, Object s3, Object s4, Object s5, Object s6, Object s7, Object s8,
			String s9, Object s10, Object s11)
	{
		StringBuilder sb = objectCollector.create(StringBuilder.class);
		try
		{
			sb.append(s1).append(s2).append(s3).append(s4).append(s5).append(s6).append(s7).append(s8);
			sb.append(s9).append(s10).append(s11);
			return sb.toString();
		}
		finally
		{
			objectCollector.dispose(sb);
		}
	}

	public static String concat(IObjectCollector objectCollector, Object s1, Object s2, Object s3, Object s4, Object s5, Object s6, Object s7, Object s8,
			String s9, Object s10, Object s11, Object s12)
	{
		StringBuilder sb = objectCollector.create(StringBuilder.class);
		try
		{
			sb.append(s1).append(s2).append(s3).append(s4).append(s5).append(s6).append(s7).append(s8);
			sb.append(s9).append(s10).append(s11).append(s12);
			return sb.toString();
		}
		finally
		{
			objectCollector.dispose(sb);
		}
	}

	public static void appendPrintable(StringBuilder sb, Object printable)
	{
		if (printable == null)
		{
			sb.append("null");
		}
		else if (printable instanceof IPrintable)
		{
			((IPrintable) printable).toString(sb);
		}
		else if (printable.getClass().isArray() && !printable.getClass().getComponentType().isPrimitive())
		{
			Arrays.toString(sb, (Object[]) printable);
		}
		else
		{
			sb.append(printable);
		}
	}

}
