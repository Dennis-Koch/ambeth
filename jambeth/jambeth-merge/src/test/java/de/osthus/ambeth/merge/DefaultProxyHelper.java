package de.osthus.ambeth.merge;

import de.osthus.ambeth.bytecode.IBytecodeEnhancer;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.ICgLibUtil;

public class DefaultProxyHelper implements IProxyHelper, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ICgLibUtil cgLibUtil;

	@Autowired(optional = true)
	protected IBytecodeEnhancer entityEnhancer;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		// Intended blank
	}

	@Override
	public Class<?> getRealType(Class<?> type)
	{
		IBytecodeEnhancer entityEnhancer = this.entityEnhancer;
		if (entityEnhancer != null)
		{
			Class<?> baseType = entityEnhancer.getBaseType(type);
			if (baseType != null)
			{
				return baseType;
			}
		}
		return cgLibUtil.getOriginalClass(type);
	}

	@Override
	public boolean objectEquals(Object leftObject, Object rightObject)
	{
		if (leftObject == null)
		{
			return rightObject == null;
		}
		if (rightObject == null)
		{
			return false;
		}
		if (leftObject == rightObject)
		{
			return true;
		}
		return leftObject.equals(rightObject);
	}
}
