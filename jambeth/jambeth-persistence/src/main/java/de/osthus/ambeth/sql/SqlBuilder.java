package de.osthus.ambeth.sql;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.osthus.ambeth.appendable.IAppendable;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.orm.XmlDatabaseMapper;
import de.osthus.ambeth.persistence.IPersistenceHelper;
import de.osthus.ambeth.util.ParamChecker;

public class SqlBuilder implements ISqlBuilder, IInitializingBean, ISqlKeywordRegistry
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	public static final Pattern dotPattern = Pattern.compile(".", Pattern.LITERAL);

	protected static final Pattern sqlEscapePattern = Pattern.compile("'", Pattern.LITERAL);

	protected final Set<Class<?>> unescapedTypes = new HashSet<Class<?>>();

	protected final Set<String> escapedNames = new HashSet<String>(0.5f);

	protected IThreadLocalObjectCollector objectCollector;

	protected IPersistenceHelper persistenceHelper;

	@Override
	public void afterPropertiesSet()
	{
		ParamChecker.assertNotNull(objectCollector, "objectCollector");
		ParamChecker.assertNotNull(persistenceHelper, "persistenceHelper");

		unescapedTypes.add(Boolean.class);
		unescapedTypes.add(Boolean.TYPE);
		unescapedTypes.add(Short.class);
		unescapedTypes.add(Short.TYPE);
		unescapedTypes.add(Integer.class);
		unescapedTypes.add(Integer.TYPE);
		unescapedTypes.add(Float.class);
		unescapedTypes.add(Float.TYPE);
		unescapedTypes.add(Long.class);
		unescapedTypes.add(Long.TYPE);
		unescapedTypes.add(Double.class);
		unescapedTypes.add(Double.TYPE);
		unescapedTypes.add(BigInteger.class);
		unescapedTypes.add(BigDecimal.class);
	}

	public void setObjectCollector(IThreadLocalObjectCollector objectCollector)
	{
		this.objectCollector = objectCollector;
	}

	public void setPersistenceHelper(IPersistenceHelper persistenceHelper)
	{
		this.persistenceHelper = persistenceHelper;
	}

	@Override
	public void registerSqlKeyword(String sqlKeyword)
	{
		escapedNames.add(sqlKeyword);
	}

	@Override
	public IAppendable appendNameValue(String name, Object value, IAppendable sb)
	{
		appendName(name, sb).append('=');
		appendValue(value, sb);
		return sb;
	}

	@Override
	public IAppendable appendNameValues(String name, List<Object> values, IAppendable sb)
	{
		IList<String> inClauses = persistenceHelper.buildStringListOfValues(values);
		boolean first = true;

		if (inClauses.size() > 1)
		{
			sb.append("(");
		}
		for (int i = inClauses.size(); i-- > 0;)
		{
			if (!first)
			{
				sb.append(" OR ");
			}
			else
			{
				first = false;
			}
			appendName(name, sb).append(" IN (").append(inClauses.get(i)).append(')');
		}
		if (inClauses.size() > 1)
		{
			sb.append(" )");
		}
		return sb;
	}

	@Override
	public IAppendable appendName(String name, IAppendable sb)
	{
		// if (escapedNames.contains(name))
		// {
		if (name.startsWith("\""))
		{
			// already escaped
			sb.append(name);
			return sb;
		}
		String dotReplacedName = dotPattern.matcher(name).replaceAll("\".\"");
		sb.append('\"').append(dotReplacedName).append('\"');
		// }
		// else
		// {
		// sb.append(name);
		// }
		return sb;
	}

	@Override
	public String[] getSchemaAndTableName(String tableName)
	{
		return XmlDatabaseMapper.splitSchemaAndName(tableName);
	}

	@Override
	public String escapeName(CharSequence name)
	{
		if (name.length() == 0 || name.charAt(0) == '\"')
		{
			// already escaped
			return name.toString();
		}
		String dotReplacedName = dotPattern.matcher(name).replaceAll("\".\"");
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
		try
		{
			sb.append('\"').append(dotReplacedName).append('\"');
			return sb.toString();

		}
		finally
		{
			tlObjectCollector.dispose(sb);
		}
	}

	@Override
	public IAppendable escapeName(CharSequence name, IAppendable sb)
	{
		if (name.length() == 0 || name.charAt(0) == '\"')
		{
			// already escaped
			sb.append(name);
			return sb;
		}
		String dotReplacedName = dotPattern.matcher(name).replaceAll(Matcher.quoteReplacement("\".\""));
		sb.append('\"').append(dotReplacedName).append('\"');
		return sb;
	}

	@Override
	public IAppendable appendValue(Object value, IAppendable sb)
	{
		if (value == null)
		{
			sb.append("NULL");
		}
		else if (isUnescapedType(value.getClass()))
		{
			sb.append(value.toString());
		}
		else
		{
			sb.append('\'');
			escapeValue(value.toString(), sb).append('\'');
		}
		return sb;
	}

	@Override
	public String escapeValue(CharSequence value)
	{
		return sqlEscapePattern.matcher(value).replaceAll("''");
	}

	@Override
	public IAppendable escapeValue(CharSequence value, IAppendable sb)
	{
		value = escapeValue(value);
		sb.append(value);
		return sb;
	}

	@Override
	public boolean isUnescapedType(Class<?> type)
	{
		return unescapedTypes.contains(type);
	}
}
