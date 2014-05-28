package de.osthus.ambeth.merge;

import de.osthus.ambeth.bytecode.IBytecodeEnhancer;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.proxy.ICgLibUtil;
import de.osthus.ambeth.typeinfo.IRelationInfoItem;

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
	public IObjRef[] getObjRefs(Object parentObj, String memberName)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IObjRef[] getObjRefs(Object parentObj, IRelationInfoItem member)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isInitialized(Object parentObj, String memberName)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isInitialized(Object parentObj, IRelationInfoItem member)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setObjRefs(Object parentObj, IRelationInfoItem member, IObjRef[] objRefs)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setUninitialized(Object parentObj, IRelationInfoItem member, IObjRef[] objRefs)
	{
		throw new UnsupportedOperationException();
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

	@Override
	public Object getValueDirect(Object parentObj, IRelationInfoItem member)
	{
		return member.getValue(parentObj);
	}

	@Override
	public void setValueDirect(Object parentObj, IRelationInfoItem member, Object value)
	{
		member.setValue(parentObj, value);
	}
}
