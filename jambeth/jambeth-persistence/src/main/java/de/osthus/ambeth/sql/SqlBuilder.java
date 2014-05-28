package de.osthus.ambeth.sql;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.persistence.IPersistenceHelper;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.StringBuilderUtil;

public class SqlBuilder implements ISqlBuilder, IInitializingBean, ISqlKeywordRegistry
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected static final Pattern dotPattern = Pattern.compile(".", Pattern.LITERAL);

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
	public Appendable appendNameValue(String name, Object value, Appendable sb)
	{
		try
		{
			appendName(name, sb).append('=');
		}
		catch (IOException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		appendValue(value, sb);
		return sb;
	}

	@Override
	public Appendable appendNameValues(String name, List<Object> values, Appendable sb)
	{
		IList<String> inClauses = this.persistenceHelper.buildStringListOfValues(values);
		boolean first = true;

		try
		{
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
		}
		catch (IOException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}

		return sb;
	}

	@Override
	public Appendable appendName(String name, Appendable sb)
	{
		// if (escapedNames.contains(name.toUpperCase()))
		// {
		String dotReplacedName = dotPattern.matcher(name).replaceAll("\".\"");
		try
		{
			sb.append('\"').append(dotReplacedName).append('\"');
		}
		catch (IOException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		// }
		// else
		// {
		// sb.append(name);
		// }
		return sb;
	}

	@Override
	public String escapeName(CharSequence name)
	{
		String dotReplacedName = dotPattern.matcher(name).replaceAll("\".\"");
		return StringBuilderUtil.concat(objectCollector.getCurrent(), "\"", dotReplacedName, "\"");
	}

	@Override
	public Appendable appendValue(Object value, Appendable sb)
	{
		try
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
		}
		catch (IOException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		return sb;
	}

	@Override
	public String escapeValue(CharSequence value)
	{
		return sqlEscapePattern.matcher(value).replaceAll("''");
	}

	@Override
	public <V extends Appendable> V escapeValue(CharSequence value, V sb)
	{
		try
		{
			value = escapeValue(value);
			sb.append(value);
		}
		catch (IOException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		return sb;
	}

	@Override
	public boolean isUnescapedType(Class<?> type)
	{
		return unescapedTypes.contains(type);
	}
}
