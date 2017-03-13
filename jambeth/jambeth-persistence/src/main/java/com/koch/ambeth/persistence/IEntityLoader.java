package com.koch.ambeth.persistence;

import java.util.List;

import com.koch.ambeth.service.cache.model.ILoadContainer;
import com.koch.ambeth.service.merge.model.IObjRef;

public interface IEntityLoader
{

	void assignInstances(List<IObjRef> orisToLoad, List<ILoadContainer> targetEntities);

	void fillVersion(List<IObjRef> orisWithoutVersion);

}
