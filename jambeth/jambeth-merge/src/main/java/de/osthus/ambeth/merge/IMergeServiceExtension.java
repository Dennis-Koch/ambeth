package de.osthus.ambeth.merge;

import java.util.List;

import de.osthus.ambeth.merge.IValueObjectConfig;
import de.osthus.ambeth.merge.model.ICUDResult;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IOriCollection;
import de.osthus.ambeth.model.IMethodDescription;

public interface IMergeServiceExtension
{
	IOriCollection merge(ICUDResult cudResult, IMethodDescription methodDescription);

	List<IEntityMetaData> getMetaData(List<Class<?>> entityTypes);

	IValueObjectConfig getValueObjectConfig(Class<?> valueType);
}
