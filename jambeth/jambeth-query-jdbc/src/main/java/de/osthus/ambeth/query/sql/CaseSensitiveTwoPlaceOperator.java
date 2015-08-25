package de.osthus.ambeth.query.sql;

import de.osthus.ambeth.appendable.IAppendable;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.filter.QueryConstants;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.IPersistenceHelper;
import de.osthus.ambeth.query.IMultiValueOperand;
import de.osthus.ambeth.query.TwoPlaceOperator;
import de.osthus.ambeth.sql.ParamsUtil;

abstract public class CaseSensitiveTwoPlaceOperator extends TwoPlaceOperator
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property(defaultValue = "true")
	protected boolean caseSensitive;

	@Autowired
	protected IConnectionDialect connectionDialect;

	protected int maxInClauseBatchThreshold;

	@Autowired
	protected ListToSqlUtil listToSqlUtil;

	@Autowired
	protected IPersistenceHelper persistenceHelper;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		maxInClauseBatchThreshold = connectionDialect.getMaxInClauseBatchThreshold();
	}

	@Override
	protected void processLeftOperandAspect(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
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
			leftOperand.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
			querySB.append(")");
			return;
		}
		if (!caseSensitive)
		{
			querySB.append("LOWER(");
		}
		super.processLeftOperandAspect(querySB, nameToValueMap, joinQuery, parameters);
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
	protected void processRightOperandAspect(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, Class<?> leftOperandFieldType,
			IList<Object> parameters)
	{
		if (supportsMultiValueOperand() && rightOperand instanceof IMultiValueOperand)
		{
			preProcessRightOperand(querySB, nameToValueMap, parameters);
			handleMultiValueOperand((IMultiValueOperand) rightOperand, querySB, nameToValueMap, joinQuery, leftOperandFieldType, parameters);
			postProcessRightOperand(querySB, nameToValueMap, parameters);
			return;
		}
		if (!caseSensitive)
		{
			querySB.append("LOWER(");
		}
		super.processRightOperandAspect(querySB, nameToValueMap, joinQuery, leftOperandFieldType, parameters);
		if (!caseSensitive)
		{
			querySB.append(')');
		}
	}

	protected void handleMultiValueOperand(IMultiValueOperand operand, IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery,
			Class<?> leftOperandFieldType, IList<Object> parameters)
	{
		@SuppressWarnings("unchecked")
		IList<IList<Object>> splitValues = (IList<IList<Object>>) nameToValueMap.get(QueryConstants.REMAINING_RIGHT_OPERAND_HANDLE);
		if (parameters == null)
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
			if (connectionDialect.isCompactMultiValueRecommended(values))
			{
				splitValues = persistenceHelper.splitValues(values, maxInClauseBatchThreshold);
				handleWithMultiValueLeftField(querySB, nameToValueMap, parameters, splitValues);
				return;
			}
			splitValues = persistenceHelper.splitValues(values);
		}

		if (!java.sql.Array.class.isAssignableFrom(leftOperandFieldType))
		{
			handleWithSingleValueLeftField(querySB, nameToValueMap, parameters, splitValues);
		}
		else
		{
			handleWithMultiValueLeftField(querySB, nameToValueMap, parameters, splitValues);
		}
	}

	protected void handleWithSingleValueLeftField(IAppendable querySB, IMap<Object, Object> nameToValueMap, IList<Object> parameters,
			IList<IList<Object>> splitValues)
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
			ParamsUtil.addParam(parameters, values.get(i));
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

	protected void handleWithMultiValueLeftField(IAppendable querySB, IMap<Object, Object> nameToValueMap, IList<Object> parameters,
			IList<IList<Object>> splitValues)
	{
		Class<?> leftOperandFieldType = getLeftOperandFieldSubType();
		connectionDialect.handleWithMultiValueLeftField(querySB, nameToValueMap, parameters, splitValues, caseSensitive, leftOperandFieldType);
		nameToValueMap.remove(QueryConstants.REMAINING_RIGHT_OPERAND_HANDLE);
	}
}