package de.osthus.ambeth.persistence.jdbc.sql;

import de.osthus.ambeth.appendable.IAppendable;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.query.IValueOperand;

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
