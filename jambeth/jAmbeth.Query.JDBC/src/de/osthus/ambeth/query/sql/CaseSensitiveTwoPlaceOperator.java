package de.osthus.ambeth.query.sql;

import java.io.IOException;
import java.util.Map;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.filter.QueryConstants;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.ArrayQueryItem;
import de.osthus.ambeth.persistence.IPersistenceHelper;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.query.IMultiValueOperand;
import de.osthus.ambeth.query.TwoPlaceOperator;
import de.osthus.ambeth.sql.ParamsUtil;
import de.osthus.ambeth.util.ParamChecker;

abstract public class CaseSensitiveTwoPlaceOperator extends TwoPlaceOperator
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected boolean caseSensitive = true;

	protected int maxInClauseBatchThreshold;

	protected ListToSqlUtil listToSqlUtil;

	protected IPersistenceHelper persistenceHelper;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(listToSqlUtil, "ListToSqlUtil");
		ParamChecker.assertNotNull(persistenceHelper, "PersistenceHelper");
	}

	public void setListToSqlUtil(ListToSqlUtil listToSqlUtil)
	{
		this.listToSqlUtil = listToSqlUtil;
	}

	public void setPersistenceHelper(IPersistenceHelper persistenceHelper)
	{
		this.persistenceHelper = persistenceHelper;
	}

	public void setCaseSensitive(boolean caseSensitive)
	{
		this.caseSensitive = caseSensitive;
	}

	@Property(name = PersistenceConfigurationConstants.MaxInClauseBatchThreshold, defaultValue = "8000")
	public void setMaxInClauseBatchThreshold(int maxInClauseBatchThreshold)
	{
		this.maxInClauseBatchThreshold = maxInClauseBatchThreshold;
	}

	@Override
	protected void processLeftOperandAspect(Appendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, Map<Integer, Object> params)
			throws IOException
	{
		boolean caseSensitive = this.caseSensitive;
		Class<?> leftOperandFieldType = getLeftOperandFieldType();
		if (supportsMultiValueOperand() && java.sql.Array.class.isAssignableFrom(leftOperandFieldType))
		{
			querySB.append("SELECT ");

			if (!caseSensitive)
			{
				querySB.append("LOWER(");
			}
			querySB.append("COLUMN_VALUE");
			if (!caseSensitive)
			{
				querySB.append(") COLUMN_VALUE");
			}
			querySB.append(" FROM TABLE(");
			leftOperand.expandQuery(querySB, nameToValueMap, joinQuery, params);
			querySB.append(")");
			return;
		}
		if (!caseSensitive)
		{
			querySB.append("LOWER(");
		}
		super.processLeftOperandAspect(querySB, nameToValueMap, joinQuery, params);
		if (!caseSensitive)
		{
			querySB.append(')');
		}
	}

	protected boolean supportsMultiValueOperand()
	{
		return false;
	}

	@Override
	protected void processRightOperandAspect(Appendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, Class<?> leftOperandFieldType,
			Map<Integer, Object> params) throws IOException
	{
		if (supportsMultiValueOperand() && rightOperand instanceof IMultiValueOperand)
		{
			preProcessRightOperand(querySB, nameToValueMap, params);
			handleMultiValueOperand((IMultiValueOperand) rightOperand, querySB, nameToValueMap, joinQuery, leftOperandFieldType, params);
			postProcessRightOperand(querySB, nameToValueMap, params);
			return;
		}
		if (!caseSensitive)
		{
			querySB.append("LOWER(");
		}
		super.processRightOperandAspect(querySB, nameToValueMap, joinQuery, leftOperandFieldType, params);
		if (!caseSensitive)
		{
			querySB.append(')');
		}
	}

	protected void handleMultiValueOperand(IMultiValueOperand operand, Appendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery,
			Class<?> leftOperandFieldType, Map<Integer, Object> params) throws IOException
	{
		@SuppressWarnings("unchecked")
		IList<IList<Object>> splitValues = (IList<IList<Object>>) nameToValueMap.get(QueryConstants.REMAINING_RIGHT_OPERAND_HANDLE);
		if (params == null)
		{
			if (splitValues != null)
			{
				throw new IllegalStateException("Must never happen");
			}
			if (!caseSensitive)
			{
				// 'Dirty' hack. This is invalid SQL but we need this only for a paging query-key
				querySB.append("LOWER(");
			}
			IList<Object> values = operand.getMultiValue(nameToValueMap);
			listToSqlUtil.expandValue(querySB, values, this, nameToValueMap);
			if (!caseSensitive)
			{
				querySB.append(')');
			}
			return;
		}
		if (splitValues == null)
		{
			IList<Object> values = operand.getMultiValue(nameToValueMap);
			if (values.size() > maxInClauseBatchThreshold)
			{
				splitValues = persistenceHelper.splitValues(values, 4000);
				handleWithMultiValueLeftField(querySB, nameToValueMap, params, splitValues);
				return;
			}
			splitValues = persistenceHelper.splitValues(values);
		}

		if (!java.sql.Array.class.isAssignableFrom(leftOperandFieldType))
		{
			handleWithSingleValueLeftField(querySB, nameToValueMap, params, splitValues);
		}
		else
		{
			handleWithMultiValueLeftField(querySB, nameToValueMap, params, splitValues);
		}
	}

	protected void handleWithSingleValueLeftField(Appendable querySB, Map<Object, Object> nameToValueMap, Map<Integer, Object> params,
			IList<IList<Object>> splitValues) throws IOException
	{
		if (splitValues.isEmpty())
		{
			return;
		}

		String placeholder;
		if (caseSensitive)
		{
			placeholder = "?";
		}
		else
		{
			placeholder = "LOWER(?)";
		}

		IList<Object> values = splitValues.get(0);
		for (int i = 0, size = values.size(); i < size; i++)
		{
			if (i != 0)
			{
				querySB.append(",");
			}
			querySB.append(placeholder);
			ParamsUtil.addParam(params, values.get(i));
		}

		if (splitValues.size() > 1)
		{
			splitValues.remove(0);
			nameToValueMap.put(QueryConstants.REMAINING_RIGHT_OPERAND_HANDLE, splitValues);
		}
		else
		{
			nameToValueMap.remove(QueryConstants.REMAINING_RIGHT_OPERAND_HANDLE);
		}
	}

	protected void handleWithMultiValueLeftField(Appendable querySB, Map<Object, Object> nameToValueMap, Map<Integer, Object> params,
			IList<IList<Object>> splitValues) throws IOException
	{
		Class<?> leftOperandFieldType;
		leftOperandFieldType = getLeftOperandFieldSubType();
		querySB.append("SELECT COLUMN_VALUE FROM (");
		if (splitValues.size() == 0)
		{
			// Special scenario with EMPTY argument
			ArrayQueryItem aqi = new ArrayQueryItem(new Object[0], leftOperandFieldType);
			ParamsUtil.addParam(params, aqi);
			querySB.append("SELECT ");
			if (!caseSensitive)
			{
				querySB.append("LOWER(");
			}
			querySB.append("COLUMN_VALUE");
			if (!caseSensitive)
			{
				querySB.append(") COLUMN_VALUE");
			}
			querySB.append(",ROWNUM FROM TABLE(?)");
		}
		else
		{
			String placeholder;
			if (caseSensitive)
			{
				placeholder = "COLUMN_VALUE";
			}
			else
			{
				placeholder = "LOWER(COLUMN_VALUE) COLUMN_VALUE";
			}

			for (int a = 0, size = splitValues.size(); a < size; a++)
			{
				IList<Object> values = splitValues.get(a);
				if (a > 0)
				{
					// A union allows us to suppress the "ROWNUM" column because table(?) will already get materialized without it
					querySB.append(" UNION ");
				}
				if (size > 1)
				{
					querySB.append('(');
				}
				ArrayQueryItem aqi = new ArrayQueryItem(values.toArray(), leftOperandFieldType);
				ParamsUtil.addParam(params, aqi);
				querySB.append("SELECT ").append(placeholder);
				if (size < 2)
				{
					// No union active
					querySB.append(",ROWNUM");
				}
				querySB.append(" FROM TABLE(?)");
				if (size > 1)
				{
					querySB.append(')');
				}
			}
		}
		querySB.append(')');
		nameToValueMap.remove(QueryConstants.REMAINING_RIGHT_OPERAND_HANDLE);
	}
}