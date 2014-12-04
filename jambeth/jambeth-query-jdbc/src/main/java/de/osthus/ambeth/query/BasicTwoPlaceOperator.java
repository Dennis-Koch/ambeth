package de.osthus.ambeth.query;

import java.util.List;
import java.util.Map;

import de.osthus.ambeth.appendable.IAppendable;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.filter.QueryConstants;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public abstract class BasicTwoPlaceOperator implements IOperator, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		// Intended blank
	}

	@Override
	public void expandQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		operate(querySB, nameToValueMap, joinQuery, parameters);
	}

	protected abstract boolean isRightValueNull(Map<Object, Object> nameToValueMap);

	protected abstract boolean isRightValueNullOrEmpty(Map<Object, Object> nameToValueMap);

	protected abstract Class<?> getLeftOperandFieldType();

	protected abstract Class<?> getLeftOperandFieldSubType();

	@SuppressWarnings("unchecked")
	protected List<Object> getRemainingLeftOperandHandle(Map<Object, Object> nameToValueMap)
	{
		return (List<Object>) nameToValueMap.get(QueryConstants.REMAINING_LEFT_OPERAND_HANDLE);
	}

	@SuppressWarnings("unchecked")
	protected List<Object> getRemainingRightOperandHandle(Map<Object, Object> nameToValueMap)
	{
		return (List<Object>) nameToValueMap.get(QueryConstants.REMAINING_RIGHT_OPERAND_HANDLE);
	}

	@Override
	public void operate(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		boolean rightValueIsNull = isRightValueNull(nameToValueMap);

		Class<?> leftOperandFieldType = getLeftOperandFieldType();

		Object outerRemainingLeftOperandHandle = nameToValueMap.remove(QueryConstants.REMAINING_LEFT_OPERAND_HANDLE);
		Object outerRemainingRightOperandHandle = nameToValueMap.remove(QueryConstants.REMAINING_RIGHT_OPERAND_HANDLE);
		Object outerConsumeRightOperandHandle = nameToValueMap.remove(QueryConstants.CONSUME_RIGHT_OPERAND_HANDLE);

		preProcessOperate(querySB, nameToValueMap, joinQuery, parameters);
		boolean loopRight = true;
		while (loopRight)
		{
			loopRight = true;

			processLeftOperandAspect(querySB, nameToValueMap, joinQuery, parameters);
			// loopLeft = getRemainingLeftOperandHandle(nameToValueMap) != null;
			expandOperatorQuery(querySB, nameToValueMap, rightValueIsNull);

			// if (loopLeft)
			// {
			// nameToValueMap.put(QueryConstants.CONSUME_RIGHT_OPERAND_HANDLE, value)
			// }
			processRightOperandAspect(querySB, nameToValueMap, joinQuery, leftOperandFieldType, parameters);
			loopRight = getRemainingRightOperandHandle(nameToValueMap) != null;

			if (loopRight)
			{
				querySB.append(" OR ");
			}
		}
		postProcessOperate(querySB, nameToValueMap, joinQuery, parameters);

		if (outerRemainingLeftOperandHandle != null)
		{
			nameToValueMap.put(QueryConstants.REMAINING_LEFT_OPERAND_HANDLE, outerRemainingLeftOperandHandle);
		}
		if (outerRemainingRightOperandHandle != null)
		{
			nameToValueMap.put(QueryConstants.REMAINING_RIGHT_OPERAND_HANDLE, outerRemainingRightOperandHandle);
		}
		if (outerConsumeRightOperandHandle != null)
		{
			nameToValueMap.put(QueryConstants.CONSUME_RIGHT_OPERAND_HANDLE, outerConsumeRightOperandHandle);
		}
	}

	protected void preProcessOperate(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		querySB.append('(');
	}

	protected void postProcessOperate(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		querySB.append(')');
	}

	protected void processLeftOperandAspect(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		preProcessLeftOperand(querySB, nameToValueMap, parameters);
		processLeftOperand(querySB, nameToValueMap, joinQuery, parameters);
		postProcessLeftOperand(querySB, nameToValueMap, parameters);
	}

	protected void processRightOperandAspect(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, Class<?> leftValueOperandType,
			IList<Object> parameters)
	{
		preProcessRightOperand(querySB, nameToValueMap, parameters);
		processRightOperand(querySB, nameToValueMap, joinQuery, leftValueOperandType, parameters);
		postProcessRightOperand(querySB, nameToValueMap, parameters);
	}

	protected abstract void processLeftOperand(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters);

	protected abstract void processRightOperand(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, Class<?> leftValueOperandType,
			IList<Object> parameters);

	protected void preProcessLeftOperand(IAppendable querySB, IMap<Object, Object> nameToValueMap, IList<Object> parameters)
	{
		// Intended blank
	}

	protected void postProcessLeftOperand(IAppendable querySB, IMap<Object, Object> nameToValueMap, IList<Object> parameters)
	{
		// Intended blank
	}

	protected void preProcessRightOperand(IAppendable querySB, IMap<Object, Object> nameToValueMap, IList<Object> parameters)
	{
		// Intended blank
	}

	protected void postProcessRightOperand(IAppendable querySB, IMap<Object, Object> nameToValueMap, IList<Object> parameters)
	{
		// Intended blank
	}

	protected abstract void expandOperatorQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean rightValueIsNull);
}
