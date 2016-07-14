package de.osthus.ambeth.query.shuang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.osthus.ambeth.filter.model.ISortDescriptor;

public class QueryUtils
{
	private static final Pattern PATTERN_START_BY = Pattern.compile("\\b(query|retrieve|count|find)(By|All)");
	private static final Pattern PATTERN_SPLITER;
	static
	{
		StringBuilder sb = new StringBuilder("(?<=(\\b|And|Or))([A-Z]\\w+?)(");
		Condition[] values = Condition.values();
		if (values.length > 0)
		{
			sb.append(values[0].toCapitalize());
		}
		for (int i = 1; i < values.length; i++)
		{
			sb.append('|').append(values[i].toCapitalize());
		}
		sb.append(")?((And|Or)(?=[A-Z])|OrderBy(?=[A-Z])|\\b)");
		PATTERN_SPLITER = Pattern.compile(sb.toString());
	}

	public static boolean canBuildQuery(String methodName)
	{
		return PATTERN_START_BY.matcher(methodName).find();
	}

	public static <T> QueryBuilderBean<T> buildQuery(String queryStr, Class<T> entityType)
	{
		FieldAnalyzer fieldExtracter = new FieldAnalyzer(entityType);
		List<OperationBean> queryBeans = extractQeryBean(queryStr, fieldExtracter);
		List<ISortDescriptor> sorts = fieldExtracter.buildSort(queryStr);
		return new QueryBuilderBean<T>(queryBeans, entityType, sorts, queryStr);
	}

	private static List<OperationBean> extractQeryBean(String queryStr, FieldAnalyzer fieldExtracter)
	{
		queryStr = PATTERN_START_BY.matcher(queryStr).replaceFirst("");
		if (queryStr.isEmpty())
		{
			return Collections.emptyList();
		}
		List<OperationBean> result = new ArrayList<OperationBean>();
		Matcher matcher = PATTERN_SPLITER.matcher(queryStr);
		while (matcher.find())
		{
			String fieldName = matcher.group(2);
			String nestedFieldName = fieldExtracter.buildNestField(fieldName);
			Condition operation = Condition.build(matcher.group(3));
			String relation = matcher.group(5);
			result.add(new OperationBean(nestedFieldName, operation, relation));
		}
		return result;
	}
}
