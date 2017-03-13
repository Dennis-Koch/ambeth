package com.koch.ambeth.merge;

import java.util.List;

import com.koch.ambeth.merge.incremental.IIncrementalMergeState;
import com.koch.ambeth.merge.model.ICUDResult;
import com.koch.ambeth.merge.model.IOriCollection;
import com.koch.ambeth.service.merge.IValueObjectConfig;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.util.model.IMethodDescription;

public interface IMergeServiceExtension
{
	IOriCollection merge(ICUDResult cudResult, IMethodDescription methodDescription);

	ICUDResult evaluateImplictChanges(ICUDResult cudResult, IIncrementalMergeState incrementalState);

	List<IEntityMetaData> getMetaData(List<Class<?>> entityTypes);

	IValueObjectConfig getValueObjectConfig(Class<?> valueType);
}
