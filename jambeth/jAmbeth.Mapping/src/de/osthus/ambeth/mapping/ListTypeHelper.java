package de.osthus.ambeth.mapping;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.typeinfo.IPropertyInfo;
import de.osthus.ambeth.typeinfo.IPropertyInfoProvider;
import de.osthus.ambeth.util.ParamChecker;

public class ListTypeHelper implements IListTypeHelper, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	private IPropertyInfoProvider propertyInfoProvider;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(propertyInfoProvider, "propertyInfoProvider");
	}

	public void setPropertyInfoProvider(IPropertyInfoProvider propertyInfoProvider)
	{
		this.propertyInfoProvider = propertyInfoProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.ambeth.mapping.IListTypeHelper#packInListType(java.util.Collection, java.lang.Class)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <L> L packInListType(Collection<?> referencedVOs, Class<L> listType)
	{
		L listTypeInst;
		try
		{
			listTypeInst = listType.newInstance();
		}
		catch (Exception e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}

		if (referencedVOs == null)
		{
			return listTypeInst;
		}

		IPropertyInfo accessor = getListTypeAccessor(listType);
		if (accessor.isWritable())
		{
			if (!accessor.getPropertyType().isAssignableFrom(referencedVOs.getClass()))
			{
				Collection<Object> targetCollection;
				Class<?> propertyType = accessor.getPropertyType();
				if (List.class.isAssignableFrom(propertyType))
				{
					targetCollection = new java.util.ArrayList<Object>(referencedVOs);
				}
				else if (Set.class.isAssignableFrom(propertyType))
				{
					targetCollection = new java.util.HashSet<Object>(referencedVOs);
				}
				else
				{
					throw new IllegalArgumentException("Collection type of '" + propertyType.getName() + "' is not supported");
				}
				referencedVOs = targetCollection;
			}
			accessor.setValue(listTypeInst, referencedVOs);
		}
		else
		{
			((Collection<Object>) accessor.getValue(listTypeInst)).addAll(referencedVOs);
		}

		return listTypeInst;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.ambeth.mapping.IListTypeHelper#unpackListType(java.lang.Object)
	 */
	@Override
	public Object unpackListType(Object item)
	{
		IPropertyInfo accessor = getListTypeAccessor(item.getClass());
		Object value = accessor.getValue(item);
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.ambeth.mapping.IListTypeHelper#isListType(java.lang.Object)
	 */
	@Override
	public boolean isListType(Class<?> type)
	{
		IPropertyInfo[] properties = propertyInfoProvider.getProperties(type);
		return properties.length == 1;
	}

	protected IPropertyInfo getListTypeAccessor(Class<?> type)
	{
		IPropertyInfo[] properties = propertyInfoProvider.getProperties(type);
		if (properties.length != 1)
		{
			throw new IllegalArgumentException("ListTypes must have exactly one property: '" + type + "'");
		}
		return properties[0];
	}
}
