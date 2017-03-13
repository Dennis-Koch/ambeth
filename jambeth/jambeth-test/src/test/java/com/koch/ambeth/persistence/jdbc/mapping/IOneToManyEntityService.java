package com.koch.ambeth.persistence.jdbc.mapping;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.koch.ambeth.cache.Cached;
import com.koch.ambeth.persistence.jdbc.mapping.models.OneToManyEntity;

public interface IOneToManyEntityService
{
	@Cached(alternateIdName = "Name")
	OneToManyEntity getOneToManyEntityByName(String name);

	@Cached(alternateIdName = "Name")
	OneToManyEntity getOneToManyEntityByNames(String... names);

	@Cached(alternateIdName = "Name")
	OneToManyEntity getOneToManyEntityByNames(List<String> names);

	@Cached(alternateIdName = "Name")
	OneToManyEntity getOneToManyEntityByNames(Collection<String> names);

	@Cached(alternateIdName = "Name")
	OneToManyEntity[] getOneToManyEntitiesByNamesReturnArray(String... names);

	@Cached(type = OneToManyEntity.class, alternateIdName = "Name")
	List<OneToManyEntity> getOneToManyEntitiesByNamesReturnList(String... names);

	@Cached(type = OneToManyEntity.class, alternateIdName = "Name")
	Set<OneToManyEntity> getOneToManyEntitiesByNamesReturnSet(String... names);

	@Cached(type = OneToManyEntity.class, alternateIdName = "Name")
	Collection<OneToManyEntity> getOneToManyEntitiesByNamesReturnCollection(String... names);

	void updateOneToManyEntity(OneToManyEntity oneToManyEntity);

	void deleteOneToManyEntity(OneToManyEntity oneToManyEntity);
}
