package de.osthus.ambeth.query;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
	public void expandQuery(Appendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, Map<Integer, Object> params) throws IOException
	{
		operate(querySB, nameToValueMap, joinQuery, params);
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
	public void operate(Appendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, Map<Integer, Object> params) throws IOException
	{
		boolean rightValueIsNull = isRightValueNull(nameToValueMap);

		Class<?> leftOperandFieldType = getLeftOperandFieldType();

		Object outerRemainingLeftOperandHandle = nameToValueMap.remove(QueryConstants.REMAINING_LEFT_OPERAND_HANDLE);
		Object outerRemainingRightOperandHandle = nameToValueMap.remove(QueryConstants.REMAINING_RIGHT_OPERAND_HANDLE);
		Object outerConsumeRightOperandHandle = nameToValueMap.remove(QueryConstants.CONSUME_RIGHT_OPERAND_HANDLE);

		preProcessOperate(querySB, nameToValueMap, joinQuery, params);
		boolean loopRight = true;
		while (loopRight)
		{
			loopRight = true;

			processLeftOperandAspect(querySB, nameToValueMap, joinQuery, params);
			// loopLeft = getRemainingLeftOperandHandle(nameToValueMap) != null;
			expandOperatorQuery(querySB, nameToValueMap, rightValueIsNull);

			// if (loopLeft)
			// {
			// nameToValueMap.put(QueryConstants.CONSUME_RIGHT_OPERAND_HANDLE, value)
			// }
			processRightOperandAspect(querySB, nameToValueMap, joinQuery, leftOperandFieldType, params);
			loopRight = getRemainingRightOperandHandle(nameToValueMap) != null;

			if (loopRight)
			{
				querySB.append(" OR ");
			}
		}
		postProcessOperate(querySB, nameToValueMap, joinQuery, params);

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

	protected void preProcessOperate(Appendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, Map<Integer, Object> params) throws IOException
	{
		querySB.append('(');
	}

	protected void postProcessOperate(Appendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, Map<Integer, Object> params)
			throws IOException
	{
		querySB.append(')');
	}

	protected void processLeftOperandAspect(Appendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, Map<Integer, Object> params)
			throws IOException
	{
		preProcessLeftOperand(querySB, nameToValueMap, params);
		processLeftOperand(querySB, nameToValueMap, joinQuery, params);
		postProcessLeftOperand(querySB, nameToValueMap, params);
	}

	protected void processRightOperandAspect(Appendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, Class<?> leftValueOperandType,
			Map<Integer, Object> params) throws IOException
	{
		preProcessRightOperand(querySB, nameToValueMap, params);
		processRightOperand(querySB, nameToValueMap, joinQuery, leftValueOperandType, params);
		postProcessRightOperand(querySB, nameToValueMap, params);
	}

	protected abstract void processLeftOperand(Appendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, Map<Integer, Object> params)
			throws IOException;

	protected abstract void processRightOperand(Appendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, Class<?> leftValueOperandType,
			Map<Integer, Object> params) throws IOException;

	protected void preProcessLeftOperand(Appendable querySB, Map<Object, Object> nameToValueMap, Map<Integer, Object> params) throws IOException
	{
		// Intended blank
	}

	protected void postProcessLeftOperand(Appendable querySB, Map<Object, Object> nameToValueMap, Map<Integer, Object> params) throws IOException
	{
		// Intended blank
	}

	protected void preProcessRightOperand(Appendable querySB, Map<Object, Object> nameToValueMap, Map<Integer, Object> params) throws IOException
	{
		// Intended blank
	}

	protected void postProcessRightOperand(Appendable querySB, Map<Object, Object> nameToValueMap, Map<Integer, Object> params) throws IOException
	{
		// Intended blank
	}

	protected abstract void expandOperatorQuery(Appendable querySB, Map<Object, Object> nameToValueMap, boolean rightValueIsNull) throws IOException;
}
