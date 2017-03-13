package com.koch.ambeth.xml.pending;

import java.util.Collection;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.xml.IReader;

public class CollectionSetterCommand extends AbstractObjectCommand implements IObjectCommand, IInitializingBean
{
	private Object object;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		// Intentionally not calling super

		ParamChecker.assertTrue(objectFuture != null || object != null, "Either ObjectFuture or Object have to be set");
		ParamChecker.assertNotNull(parent, "Parent");
		ParamChecker.assertParamOfType(parent, "Parent", Collection.class);
	}

	public void setObject(Object object)
	{
		this.object = object;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute(IReader reader)
	{
		Object value = objectFuture != null ? objectFuture.getValue() : object;
		((Collection<Object>) parent).add(value);
	}
}
