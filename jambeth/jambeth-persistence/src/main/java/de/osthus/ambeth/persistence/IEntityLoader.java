package de.osthus.ambeth.persistence;

import java.util.List;

import de.osthus.ambeth.cache.model.ILoadContainer;
import de.osthus.ambeth.merge.model.IObjRef;

public interface IEntityLoader
{

	void assignInstances(List<IObjRef> orisToLoad, List<ILoadContainer> targetEntities);

	void fillVersion(List<IObjRef> orisWithoutVersion);

}
