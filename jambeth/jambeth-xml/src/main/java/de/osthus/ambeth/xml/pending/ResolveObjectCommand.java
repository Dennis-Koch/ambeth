package de.osthus.ambeth.xml.pending;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.xml.IReader;

public class ResolveObjectCommand implements IObjectCommand, IInitializingBean
{
	protected IObjectFuture objectFuture;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(objectFuture, "ObjectFuture");
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

	public void setParent(Object parent)
	{
		// NoOp
	}

	@Override
	public void execute(IReader reader)
	{
		// NoOp
	}
}
