package com.koch.ambeth.query.squery;

/*-
 * #%L
 * jambeth-query
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.koch.ambeth.filter.ISortDescriptor;
import com.koch.ambeth.filter.SortDescriptor;
import com.koch.ambeth.filter.SortDirection;

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
	 * analyze the nestField, e.g:
	 * 
	 * SomeFieldNestField -> SomeField.NestField
	 * 
	 * @param fieldExpression
	 * @return separated nest field by dot
	 */
	public String buildNestField(String fieldExpression)
	{
		String result = this.prepareFieldName(fieldExpression, clazz);
		if (result == null)
		{
			throw new InvalidateSqueryNameException(fieldExpression, clazz);
		}
		return result;
	}

	private String prepareFieldName(String partFieldName, Class<?> clazz)
	{
		SortedSet<String> getterNames = getGetterNames(clazz);
		if (getterNames.contains(partFieldName))
		{
			return partFieldName;
		}
		for (String getterStr : getterNames)
		{
			if (partFieldName.startsWith(getterStr))
			{
				Class<?> returnType = this.getReturnType(getterStr, clazz);
				String subPartFieldName = partFieldName.substring(getterStr.length());
				String subName = this.prepareFieldName(subPartFieldName, returnType);
				if (subName != null)
				{
					return getterStr + "." + subName;
				}
			}
		}
		return null;
	}

	/**
	 * retrive all the getter names, and remove the get part
	 * 
	 * e.g: Company getCompany(); -> "Company"
	 * 
	 * @param clazz
	 *            be retrived type
	 * @return all the getter names delete "get"
	 */
	private SortedSet<String> getGetterNames(Class<?> clazz)
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
		if (queryStr == null || queryStr.isEmpty())
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
