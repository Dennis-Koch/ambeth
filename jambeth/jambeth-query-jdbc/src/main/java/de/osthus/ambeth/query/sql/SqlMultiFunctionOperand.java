package de.osthus.ambeth.query.sql;

import de.osthus.ambeth.appendable.IAppendable;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.query.IMultiValueOperand;
import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.util.ParamChecker;

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
