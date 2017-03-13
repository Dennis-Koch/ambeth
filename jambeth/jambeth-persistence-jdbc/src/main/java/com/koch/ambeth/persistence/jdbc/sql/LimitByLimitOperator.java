package com.koch.ambeth.persistence.jdbc.sql;

import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IValueOperand;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;

public class LimitByLimitOperator implements IOperand
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property
	protected IOperand operand;

	@Property
	protected IValueOperand valueOperand;

	@Override
	public void expandQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		Object value = valueOperand.getValue(nameToValueMap);
		if (value == null || ((Number) value).intValue() == 0)
		{
			return;
		}
		querySB.append("LIMIT ");
		querySB.append(value.toString());
	}
}
