package com.koch.ambeth.merge.service;

import java.util.List;

import com.koch.ambeth.merge.model.ICUDResult;
import com.koch.ambeth.merge.model.IOriCollection;
import com.koch.ambeth.service.merge.IValueObjectConfig;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.util.annotation.XmlType;
import com.koch.ambeth.util.model.IMethodDescription;

@XmlType
public interface IMergeService
{
	IOriCollection merge(ICUDResult cudResult, IMethodDescription methodDescription);

	List<IEntityMetaData> getMetaData(List<Class<?>> entityTypes);

	IValueObjectConfig getValueObjectConfig(Class<?> valueType);
}
