package com.koch.ambeth.query.jdbc.sql;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.query.IMultiValueOperand;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;

public class SqlMultiFunctionOperand implements IOperand, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected ListToSqlUtil listToSqlUtil;

	protected IMultiValueOperand multiValueOperand;

	protected String name;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(listToSqlUtil, "listToSqlUtil");
		ParamChecker.assertNotNull(multiValueOperand, "multiValueOperand");
		ParamChecker.assertNotNull(name, "name");
	}

	public void setListToSqlUtil(ListToSqlUtil listToSqlUtil)
	{
		this.listToSqlUtil = listToSqlUtil;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setMultiValueOperand(IMultiValueOperand multiValueOperand)
	{
		this.multiValueOperand = multiValueOperand;
	}

	@Override
	public void expandQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		IList<Object> items = multiValueOperand.getMultiValue(nameToValueMap);
		ListToSqlUtil listToSqlUtil = this.listToSqlUtil;
		String name = this.name;
		for (int a = 0, size = items.size(); a < size; a++)
		{
			Object item = items.get(a);
			querySB.append(name).append('(');
			listToSqlUtil.expandValue(querySB, item, this, nameToValueMap);
			querySB.append(')');
		}
	}
}
