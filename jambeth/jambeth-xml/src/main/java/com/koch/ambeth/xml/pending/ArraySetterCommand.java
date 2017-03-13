package com.koch.ambeth.xml.pending;

import java.lang.reflect.Array;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.xml.IReader;

public class ArraySetterCommand extends AbstractObjectCommand implements IObjectCommand, IInitializingBean
{
	protected Integer index;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertTrue(parent.getClass().isArray(), "Parent has to be an array");
		ParamChecker.assertNotNull(index, "Index");
	}

	public void setIndex(Integer index)
	{
		this.index = index;
	}

	@Override
	public void execute(IReader reader)
	{
		Object value = objectFuture.getValue();
		Array.set(parent, index, value);
	}
}
