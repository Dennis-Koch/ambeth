package com.koch.ambeth.persistence.blueprint;

import java.util.Collection;

import com.koch.ambeth.merge.orm.blueprint.IEntityTypeBlueprint;
import com.koch.ambeth.model.IAbstractEntity;

public interface EntityTypeBlueprint extends IAbstractEntity, IEntityTypeBlueprint
{
	@Override
	Collection<EntityPropertyBlueprint> getProperties();

	@Override
	Collection<EntityAnnotationBlueprint> getAnnotations();
}
