package com.koch.ambeth.persistence.jdbc.mapping;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.koch.ambeth.cache.Cached;
import com.koch.ambeth.persistence.jdbc.mapping.models.SelfReferencingEntity;

public interface ISelfReferencingEntityService
{
	@Cached(alternateIdName = "Name")
	SelfReferencingEntity getSelfReferencingEntityByName(String name);

	@Cached(alternateIdName = "Name")
	SelfReferencingEntity getSelfReferencingEntityByNames(String... names);

	@Cached(alternateIdName = "Name")
	SelfReferencingEntity getSelfReferencingEntityByNames(List<String> names);

	@Cached(alternateIdName = "Name")
	SelfReferencingEntity getSelfReferencingEntityByNames(Collection<String> names);

	@Cached(alternateIdName = "Name")
	SelfReferencingEntity[] getSelfReferencingEntitiesByNamesReturnArray(String... names);

	@Cached(type = SelfReferencingEntity.class, alternateIdName = "Name")
	List<SelfReferencingEntity> getSelfReferencingEntitiesByNamesReturnList(String... names);

	@Cached(type = SelfReferencingEntity.class, alternateIdName = "Name")
	Set<SelfReferencingEntity> getSelfReferencingEntitiesByNamesReturnSet(String... names);

	@Cached(type = SelfReferencingEntity.class, alternateIdName = "Name")
	Collection<SelfReferencingEntity> getSelfReferencingEntitiesByNamesReturnCollection(String... names);

	void updateSelfReferencingEntity(SelfReferencingEntity selfReferencingEntity);

	void deleteSelfReferencingEntity(SelfReferencingEntity selfReferencingEntity);
}
