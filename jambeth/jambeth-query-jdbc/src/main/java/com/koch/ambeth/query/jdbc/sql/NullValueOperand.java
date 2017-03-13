package com.koch.ambeth.query.jdbc.sql;

import java.util.Map;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.query.IMultiValueOperand;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IValueOperand;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.EmptyList;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;

public class NullValueOperand implements IOperand, IValueOperand, IMultiValueOperand {
	public static final NullValueOperand INSTANCE = new NullValueOperand();

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public boolean isNull(Map<Object, Object> nameToValueMap) {
		return true;
	}

	@Override
	public boolean isNullOrEmpty(Map<Object, Object> nameToValueMap) {
		return true;
	}

	@Override
	public Object getValue(Map<Object, Object> nameToValueMap) {
		return null;
	}

	@Override
	public IList<Object> getMultiValue(Map<Object, Object> nameToValueMap) {
		return EmptyList.createTypedEmptyList(Object.class);
	}

	@Override
	public void expandQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap,
			boolean joinQuery, IList<Object> parameters) {
		querySB.append("NULL");
	}
}
