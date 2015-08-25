package de.osthus.ambeth.persistence;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import de.osthus.ambeth.appendable.AppendableStringBuilder;
import de.osthus.ambeth.appendable.IAppendable;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.sql.ISqlBuilder;
import de.osthus.ambeth.sql.ParamsUtil;

public class PersistenceHelper implements IPersistenceHelper, IInitializingBean
{
	@Property(name = PersistenceConfigurationConstants.BatchSize, defaultValue = "1000")
	protected int batchSize;

	@Property(name = PersistenceConfigurationConstants.PreparedBatchSize, defaultValue = "1000")
	protected int preparedBatchSize;

	protected int maxInClauseBatchThreshold;

	@Autowired
	protected IConnectionDialect connectionDialect;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected ISqlBuilder sqlBuilder;

	@Override
	public void afterPropertiesSet()
	{
		if (batchSize < 1)
		{
			throw new IllegalArgumentException("BatchSize must be >= 1: '" + PersistenceConfigurationConstants.BatchSize + "'");
		}
		if (preparedBatchSize < 1)
		{
			throw new IllegalArgumentException("PreparedBatchSize must be >= 1: '" + PersistenceConfigurationConstants.PreparedBatchSize + "'");
		}
		maxInClauseBatchThreshold = connectionDialect.getMaxInClauseBatchThreshold();
	}

	@Override
	public IList<IList<Object>> splitValues(Collection<?> ids)
	{
		int currentBatchSize = 0, batchSize = preparedBatchSize;

		IList<IList<Object>> splittedLists = new ArrayList<IList<Object>>(ids.size() / batchSize + 1);

		IList<Object> splitList = null;

		for (Object value : ids)
		{
			if (splitList == null || currentBatchSize >= batchSize)
			{
				splitList = new ArrayList<Object>(batchSize);
				splittedLists.add(splitList);
				currentBatchSize = 0;
			}
			currentBatchSize++;
			splitList.add(value);
		}
		return splittedLists;
	}

	@Override
	public IList<IList<Object>> splitValues(List<?> values)
	{
		return splitValues(values, preparedBatchSize);
	}

	@Override
	public IList<IList<Object>> splitValues(List<?> values, int batchSize)
	{
		IList<IList<Object>> splittedLists = new ArrayList<IList<Object>>(values.size() / batchSize + 1);

		int currentBatchSize = 0;

		IList<Object> splitList = null;

		for (int a = 0, size = values.size(); a < size; a++)
		{
			Object value = values.get(a);
			if (splitList == null || currentBatchSize >= batchSize)
			{
				splitList = new ArrayList<Object>(Math.min(size - a, batchSize));
				splittedLists.add(splitList);
				currentBatchSize = 0;
			}
			currentBatchSize++;
			splitList.add(value);
		}
		return splittedLists;
	}

	@Override
	public IList<String> buildStringListOfValues(List<?> values)
	{
		AppendableStringBuilder sb = objectCollector.create(AppendableStringBuilder.class);
		try
		{
			int currentBatchSize = 0;
			ArrayList<String> sqlStrings = new ArrayList<String>();

			boolean first = true;
			Iterator<?> iter = null;
			try
			{
				iter = values.iterator();
				while (iter.hasNext())
				{
					Object value = iter.next();
					if (!first)
					{
						sb.append(',');
					}
					else
					{
						first = false;
					}
					sqlBuilder.appendValue(value, sb);
					if (++currentBatchSize >= batchSize)
					{
						sqlStrings.add(sb.toString());
						currentBatchSize = 0;
						sb.reset();
						first = true;
					}
				}
			}
			finally
			{
				if (iter != null)
				{
					iter = null;
				}
			}
			if (sb.length() > 0)
			{
				sqlStrings.add(sb.toString());
				sb.reset();
			}

			return sqlStrings;
		}
		finally
		{
			objectCollector.dispose(sb);
		}
	}

	@Override
	public String buildStringOfValues(List<?> values)
	{
		AppendableStringBuilder sb = objectCollector.create(AppendableStringBuilder.class);
		try
		{
			return appendStringOfValues(values, sb).toString();
		}
		finally
		{
			objectCollector.dispose(sb);
		}
	}

	@Override
	public IAppendable appendStringOfValues(List<?> values, IAppendable sb)
	{
		boolean first = true;

		for (int a = 0, size = values.size(); a < size; a++)
		{
			Object value = values.get(a);
			if (!first)
			{
				sb.append(',');
			}
			else
			{
				first = false;
			}
			sqlBuilder.appendValue(value, sb);
		}
		return sb;
	}

	@Override
	public IAppendable appendSplittedValues(String idColumnName, Class<?> fieldType, List<?> ids, List<Object> parameters, IAppendable sb)
	{
		if (ids.size() > maxInClauseBatchThreshold)
		{
			// TODO: Assumption that array types are always with a length of 4000 here. Should be evaluated by existing data types and their length
			IList<IList<Object>> splitValues = splitValues(ids, 4000);
			sqlBuilder.appendName(idColumnName, sb);
			sb.append(" IN (SELECT COLUMN_VALUE FROM (");
			for (int a = 0, size = splitValues.size(); a < size; a++)
			{
				IList<Object> values = splitValues.get(a);
				if (a > 0)
				{
					// A union allows us to suppress the "ROWNUM" column because table(?) will already get materialized without it
					sb.append(" UNION ");
				}
				if (size > 1)
				{
					sb.append('(');
				}
				ArrayQueryItem aqi = new ArrayQueryItem(values.toArray(), fieldType);
				ParamsUtil.addParam(parameters, aqi);
				sb.append("SELECT COLUMN_VALUE");
				if (size < 2)
				{
					// No union active
					sb.append(",ROWNUM");
				}
				sb.append(" FROM TABLE(?)");
				if (size > 1)
				{
					sb.append(')');
				}
			}
			sb.append("))");
			return sb;
		}
		IList<IList<Object>> splittedIdsList = splitValues(ids);
		if (splittedIdsList.size() > 1)
		{
			sb.append('(');
		}
		for (int a = 0, size = splittedIdsList.size(); a < size; a++)
		{
			IList<Object> splittedIds = splittedIdsList.get(a);

			if (a > 0)
			{
				sb.append(" OR ");
			}
			sqlBuilder.appendName(idColumnName, sb);
			sb.append(" IN (");
			for (int b = 0, sizeB = splittedIds.size(); b < sizeB; b++)
			{
				Object id = splittedIds.get(b);
				if (b > 0)
				{
					sb.append(',');
				}
				sb.append('?');
				ParamsUtil.addParam(parameters, id);
			}
			sb.append(')');
		}
		if (splittedIdsList.size() > 1)
		{
			sb.append(')');
		}
		return sb;
	}
}
