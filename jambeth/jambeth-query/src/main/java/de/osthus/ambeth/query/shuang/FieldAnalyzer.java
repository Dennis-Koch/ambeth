package de.osthus.ambeth.query.shuang;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.osthus.ambeth.filter.model.ISortDescriptor;
import de.osthus.ambeth.filter.model.SortDescriptor;
import de.osthus.ambeth.filter.model.SortDirection;

public final class FieldAnalyzer
{
	private Class<?> clazz;
	private Map<Class<?>, SortedSet<String>> classMapField = new ConcurrentHashMap<Class<?>, SortedSet<String>>();
	private static final Pattern PATTERN_DIRECTION = Pattern.compile("(\\w+?)((Asc|Desc)(?=[A-Z]?)|\\b)");
	private static final Pattern PATTERN_ORDER_BY = Pattern.compile(".*[a-z](Sort|Order)By([A-Z].*)$");

	public FieldAnalyzer(Class<?> clazz)
	{
		this.clazz = clazz;
	}

	/**
	 * SomeFieldNestField -> SomeField.NestField
	 * 
	 * @param nestFields
	 * @return
	 */
	public String buildNestField(String nestFields)
	{
		return this.prepareFieldName(nestFields, clazz);
	}

	private String prepareFieldName(String nestFields, Class<?> clazz)
	{
		SortedSet<String> methodNames = getMethodNames(clazz);
		String findName = null;
		for (String name : methodNames)
		{
			if (nestFields.startsWith(name))
			{
				findName = name;
				break;
			}
		}
		if (findName == null)
		{
			NoSuchFieldException e = new NoSuchFieldException(nestFields);
			throw new RuntimeException(e);
		}
		if (Objects.equals(findName, nestFields))
		{
			return nestFields;
		}
		else
		{
			Class<?> returnType = this.getReturnType(findName, clazz);
			return findName + "." + this.prepareFieldName(nestFields.substring(findName.length()), returnType);
		}
	}

	private SortedSet<String> getMethodNames(Class<?> clazz)
	{
		SortedSet<String> names = classMapField.get(clazz);
		if (names != null)
		{
			return names;
		}
		Method[] methods = clazz.getMethods();
		Comparator<String> comparator = new Comparator<String>()
		{
			@Override
			public int compare(String o1, String o2)
			{
				int compareValue = o2.length() - o1.length();
				if (compareValue == 0)
				{
					compareValue = o1.compareTo(o2);
				}
				return compareValue;
			}
		};
		names = new TreeSet<String>(comparator);
		String prefix = "get";
		for (Method method : methods)
		{
			String name = method.getName();
			if (name.startsWith(prefix))
			{
				names.add(name.substring(prefix.length()));
			}
		}
		classMapField.put(clazz, names);
		return names;
	}

	private Class<?> getReturnType(String fieldName, Class<?> clazz)
	{
		char firstChar = fieldName.charAt(0);
		if (Character.isUpperCase(firstChar))
		{
			fieldName = Character.toLowerCase(firstChar) + fieldName.substring(1);
		}
		String gettterName = "get" + Character.toUpperCase(firstChar) + fieldName.substring(1);
		Class<?> result = null;
		try
		{
			Class<?> type = clazz.getMethod(gettterName).getReturnType();
			if (type.isArray())
			{
				result = type.getComponentType();
			}
			else if (Iterable.class.isAssignableFrom(type))
			{
				ParameterizedType genericType = (ParameterizedType) clazz.getDeclaredField(fieldName).getGenericType();
				result = (Class<?>) genericType.getActualTypeArguments()[0];
			}
			else
			{
				result = type;
			}
		}
		catch (Exception e)
		{
			throw new AssertionError("impossible error", e);
		}
		return result;
	}

	public List<ISortDescriptor> buildSort(String queryStr)
	{
		if (queryStr == null)
		{
			return Collections.emptyList();
		}
		Matcher matcherOrderBy = PATTERN_ORDER_BY.matcher(queryStr);
		if (!matcherOrderBy.find())
		{
			return Collections.emptyList();
		}
		List<ISortDescriptor> result = new ArrayList<ISortDescriptor>();
		String orderByStr = matcherOrderBy.group(2);

		Matcher matcher = PATTERN_DIRECTION.matcher(orderByStr);
		while (matcher.find())
		{
			String fieldName = matcher.group(1);
			String nestFieldName = this.buildNestField(fieldName);
			String dirStr = matcher.group(3);
			SortDirection direction = "Desc".equals(dirStr) ? SortDirection.DESCENDING : SortDirection.ASCENDING;
			SortDescriptor sort = new SortDescriptor();
			sort.setMember(nestFieldName);
			sort.setSortDirection(direction);
			result.add(sort);
		}
		return result;
	}
}
