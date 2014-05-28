package de.osthus.ambeth.query;

import java.util.Collection;
import java.util.List;

import de.osthus.ambeth.annotation.Find;
import de.osthus.ambeth.annotation.Merge;
import de.osthus.ambeth.proxy.Service;

@Service(IQueryEntityCRUD.class)
public interface IQueryEntityCRUD
{
	@Find(entityType = QueryEntity.class, queryName = "myQuery1")
	List<QueryEntity> getAllQueryEntities();

	@Merge
	void IwantAFunnySaveMethod(Object... queryEntities);

	@Merge
	void IwantAFunnySaveMethod(Collection<?> queryEntities);
}
