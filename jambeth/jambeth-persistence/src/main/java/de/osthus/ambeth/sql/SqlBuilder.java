package de.osthus.ambeth.sql;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import de.osthus.ambeth.appendable.IAppendable;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.orm.XmlDatabaseMapper;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.IPersistenceHelper;

public class SqlBuilder implements ISqlBuilder, IInitializingBean, ISqlKeywordRegistry
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected static final Pattern sqlEscapePattern = Pattern.compile("'", Pattern.LITERAL);

	protected final Set<Class<?>> unescapedTypes = new HashSet<Class<?>>();

	protected final Set<String> escapedNames = new HashSet<String>(0.5f);

	@Autowired
	protected IConnectionDialect connectionDialect;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IPersistenceHelper persistenceHelper;

	@Override
	public void afterPropertiesSet()
	{
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
	public String escapeName(CharSequence symbolName)
	{
		return connectionDialect.escapeName(symbolName);
	}

	@Override
	public IAppendable escapeName(CharSequence symbolName, IAppendable sb)
	{
		return connectionDialect.escapeName(symbolName, sb);
	}

	@Override
	public String escapeSchemaAndSymbolName(CharSequence schemaName, CharSequence symbolName)
	{
		return connectionDialect.escapeSchemaAndSymbolName(schemaName, symbolName);
	}

	@Override
	public IAppendable appendNameValue(CharSequence name, Object value, IAppendable sb)
	{
		appendName(name, sb).append('=');
		appendValue(value, sb);
		return sb;
	}

	@Override
	public IAppendable appendNameValues(CharSequence name, List<Object> values, IAppendable sb)
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
	public IAppendable appendName(CharSequence name, IAppendable sb)
	{
		sb.append(connectionDialect.escapeName(name));
		return sb;
	}

	@Override
	public String[] getSchemaAndTableName(String tableName)
	{
		return XmlDatabaseMapper.splitSchemaAndName(tableName);
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
