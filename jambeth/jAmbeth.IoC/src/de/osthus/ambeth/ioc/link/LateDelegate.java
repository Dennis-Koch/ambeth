package de.osthus.ambeth.ioc.link;

import de.osthus.ambeth.util.IDelegate;
import de.osthus.ambeth.util.IDelegateFactory;

public class LateDelegate
{
	protected String methodName;

	protected Class<?> delegateType;

	protected IDelegateFactory delegateFactory;

	public LateDelegate(Class<?> delegateType, String methodName, IDelegateFactory delegateFactory)
	{
		this.methodName = methodName;
		this.delegateType = delegateType;
		this.delegateFactory = delegateFactory;
	}

	public IDelegate getDelegate(Class<?> delegateType, Object target)
	{
		if (this.delegateType != null)
		{
			delegateType = this.delegateType;
		}
		return delegateFactory.createDelegate(delegateType, target, methodName);
	}
}
