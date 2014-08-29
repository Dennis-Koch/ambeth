package de.osthus.ambeth.query.sql;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import de.osthus.ambeth.collections.EmptyList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.query.IMultiValueOperand;
import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.query.IValueOperand;

public class NullValueOperand implements IOperand, IValueOperand, IMultiValueOperand
{
	public static final NullValueOperand INSTANCE = new NullValueOperand();

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public boolean isNull(Map<Object, Object> nameToValueMap)
	{
		return true;
	}

	@Override
	public boolean isNullOrEmpty(Map<Object, Object> nameToValueMap)
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.ambeth.query.sql.IValueOperand#getValue(java.util.Map)
	 */
	@Override
	public Object getValue(Map<Object, Object> nameToValueMap)
	{
		return null;
	}

	@Override
	public IList<Object> getMultiValue(Map<Object, Object> nameToValueMap)
	{
		return EmptyList.createTypedEmptyList(Object.class);
	}

	@Override
	public void expandQuery(Appendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, List<Object> parameters) throws IOException
	{
		querySB.append("NULL");
	}
}
