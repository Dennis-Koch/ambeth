package com.koch.ambeth.persistence.jdbc.alternateid;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.koch.ambeth.cache.Cached;

public interface IAlternateIdEntityService
{
	@Cached(alternateIdName = "Name")
	AlternateIdEntity getAlternateIdEntityByName(String name);

	@Cached(alternateIdName = "Name")
	AlternateIdEntity getAlternateIdEntityByNames(String... names);

	@Cached(alternateIdName = "Name")
	AlternateIdEntity getAlternateIdEntityByNames(List<String> names);

	@Cached(alternateIdName = "Name")
	AlternateIdEntity getAlternateIdEntityByNames(Collection<String> names);

	@Cached(alternateIdName = "Name")
	AlternateIdEntity[] getAlternateIdEntitiesByNamesReturnArray(String... names);

	@Cached(type = AlternateIdEntity.class, alternateIdName = "Name")
	List<AlternateIdEntity> getAlternateIdEntitiesByNamesReturnList(String... names);

	@Cached(type = AlternateIdEntity.class, alternateIdName = "Name")
	Set<AlternateIdEntity> getAlternateIdEntitiesByNamesReturnSet(String... names);

	@Cached(type = AlternateIdEntity.class, alternateIdName = "Name")
	Collection<AlternateIdEntity> getAlternateIdEntitiesByNamesReturnCollection(String... names);

	void updateAlternateIdEntity(AlternateIdEntity alternateIdEntity);

	void deleteAlternateIdEntity(AlternateIdEntity alternateIdEntity);
}
