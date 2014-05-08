package de.osthus.ambeth.xml.pending;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.util.ParamChecker;

public abstract class AbstractObjectCommand implements IObjectCommand, IInitializingBean
{
	protected IObjectFuture objectFuture;

	protected Object parent;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(objectFuture, "ObjectFuture");
		ParamChecker.assertNotNull(parent, "Parent");
	}

	@Override
	public IObjectFuture getObjectFuture()
	{
		return objectFuture;
	}

	public void setObjectFuture(IObjectFuture objectFuture)
	{
		this.objectFuture = objectFuture;
	}

	public Object getParent()
	{
		return parent;
	}

	public void setParent(Object parent)
	{
		this.parent = parent;
	}
}
