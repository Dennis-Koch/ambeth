package de.osthus.ambeth.persistence.jdbc;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.typeinfo.IRelationProvider;

public interface IEmbeddedProvider extends IRelationProvider, IInitializingBean
{
	void registerEmbeddedType(Class<?> type);

	void unregisterEmbeddedType(Class<?> type);

	void registerEmbeddedPackage(String packageName);

	void unregisterEmbeddedPackage(String packageName);
}
