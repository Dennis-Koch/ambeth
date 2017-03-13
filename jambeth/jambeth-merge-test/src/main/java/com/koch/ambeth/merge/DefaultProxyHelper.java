package com.koch.ambeth.merge;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.bytecode.IBytecodeEnhancer;
import com.koch.ambeth.ioc.proxy.ICgLibUtil;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IProxyHelper;

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
