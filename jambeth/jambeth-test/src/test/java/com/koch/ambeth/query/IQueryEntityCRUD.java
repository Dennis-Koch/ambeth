package com.koch.ambeth.query;

import java.util.Collection;
import java.util.List;

import com.koch.ambeth.service.proxy.Service;
import com.koch.ambeth.util.annotation.Find;
import com.koch.ambeth.util.annotation.Merge;

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
