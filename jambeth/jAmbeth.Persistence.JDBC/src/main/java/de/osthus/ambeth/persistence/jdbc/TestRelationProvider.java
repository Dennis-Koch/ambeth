package de.osthus.ambeth.persistence.jdbc;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.typeinfo.RelationProvider;

public class TestRelationProvider extends RelationProvider implements IEmbeddedProvider, IInitializingBean
{
	protected Set<Class<?>> embeddedTypes = new HashSet<Class<?>>();
	protected Set<String> embeddedPackages = new HashSet<String>();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
	}

	@Override
	public boolean isEntityType(Class<?> type)
	{
		if (!super.isEntityType(type))
		{
			return false;
		}
		if (embeddedTypes.contains(type))
		{
			return false;
		}
		Iterator<String> packages = embeddedPackages.iterator();
		while (packages.hasNext())
		{
			if (type.getName().startsWith(packages.next()))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public void registerEmbeddedType(Class<?> type)
	{
		if (type != null)
		{
			embeddedTypes.add(type);
		}
	}

	@Override
	public void unregisterEmbeddedType(Class<?> type)
	{
		embeddedTypes.remove(type);
	}

	@Override
	public void registerEmbeddedPackage(String packageName)
	{
		if (packageName != null)
		{
			embeddedPackages.add(packageName);
		}
	}

	@Override
	public void unregisterEmbeddedPackage(String packageName)
	{
		embeddedPackages.remove(packageName);
	}
}
