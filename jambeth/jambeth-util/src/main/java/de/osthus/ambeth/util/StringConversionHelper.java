package de.osthus.ambeth.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.osthus.ambeth.objectcollector.IObjectCollector;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;

public final class StringConversionHelper
{
	private static final Pattern insertUnderscoreBeforeUppercaseLetterPattern = Pattern.compile("(.+[^A-ZÄÖÜ_])([A-ZÄÖÜ].*)");

	private static final Pattern insertUnderscoreBeforeNumbersPattern = Pattern.compile("(.+[^\\d_])(\\d+.*)");

	private StringConversionHelper()
	{
		// Intended blank
	}

	public static String upperCaseFirst(IObjectCollector objectCollector, String s)
	{
		if (s == null || s.isEmpty())
		{
			return "";
		}
		char firstChar = s.charAt(0);
		if (Character.isUpperCase(firstChar))
		{
			return s;
		}
		return StringBuilderUtil.concat(objectCollector.getCurrent(), Character.toUpperCase(firstChar), s.substring(1));
	}

	public static String lowerCaseFirst(IObjectCollector objectCollector, String s)
	{
		if (s == null || s.isEmpty())
		{
			return "";
		}
		char firstChar = s.charAt(0);
		if (Character.isLowerCase(firstChar))
		{
			return s;
		}
		return StringBuilderUtil.concat(objectCollector.getCurrent(), Character.toLowerCase(firstChar), s.substring(1));
	}

	public static String entityNameToPlural(IObjectCollector objectCollector, String entityName)
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		Character lastChar = entityName.charAt(entityName.length() - 1);
		if (lastChar == 'y')
		{
			return StringBuilderUtil.concat(tlObjectCollector, entityName.substring(0, entityName.length() - 1), "ies");
		}
		else if (lastChar == 's')
		{
			return StringBuilderUtil.concat(tlObjectCollector, entityName, "es");
		}
		else
		{
			return StringBuilderUtil.concat(tlObjectCollector, entityName, "s");
		}
	}

	public static String pluralToEntityName(IObjectCollector objectCollector, String plural)
	{
		if (plural.endsWith("ies"))
		{
			return StringBuilderUtil.concat(objectCollector.getCurrent(), plural.substring(0, plural.length() - 3), "y");
		}
		else if (plural.endsWith("ses"))
		{
			return plural.substring(0, plural.length() - 2);
		}
		else if (plural.endsWith("s"))
		{
			return plural.substring(0, plural.length() - 1);
		}
		return plural;
	}

	public static String camelCaseToUnderscore(IObjectCollector objectCollector, String value)
	{
		return insertUnderscoreBeforeUppercaseLetter(objectCollector, value).toUpperCase();
	}

	public static String underscoreToCamelCase(IObjectCollector objectCollector, String value)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public static String insertUnderscoreBeforeUppercaseLetter(IObjectCollector objectCollector, String value)
	{
		while (true)
		{
			Matcher matcher = insertUnderscoreBeforeUppercaseLetterPattern.matcher(value);
			if (!matcher.matches())
			{
				return value;
			}
			value = StringBuilderUtil.concat(objectCollector.getCurrent(), matcher.group(1), "_", matcher.group(2));
		}
	}

	public static String insertUnderscoreBeforeNumbers(IObjectCollector objectCollector, String value)
	{
		while (true)
		{
			Matcher matcher = insertUnderscoreBeforeNumbersPattern.matcher(value);
			if (!matcher.matches())
			{
				return value;
			}
			value = StringBuilderUtil.concat(objectCollector.getCurrent(), matcher.group(1), "_", matcher.group(2));
		}
	}

	public static String implode(IObjectCollector objectCollector, String[] parts, String glue)
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
		try
		{
			boolean toGlue = false;
			for (String part : parts)
			{
				if (toGlue)
				{
					sb.append(glue);
				}
				toGlue = true;
				sb.append(part);
			}
			return sb.toString();
		}
		finally
		{
			tlObjectCollector.dispose(sb);
		}
	}

	public static boolean hasChar(CharSequence cs, char oneChar)
	{
		for (int a = 0, size = cs.length(); a < size; a++)
		{
			if (cs.charAt(a) == oneChar)
			{
				return true;
			}
		}
		return false;
	}
}