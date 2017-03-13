package com.koch.ambeth.persistence.blueprint;

import java.util.Collection;

import com.koch.ambeth.merge.orm.blueprint.IEntityAnnotationBlueprint;
import com.koch.ambeth.model.IAbstractEntity;

public interface EntityAnnotationBlueprint extends IAbstractEntity, IEntityAnnotationBlueprint
{
	@Override
	Collection<EntityAnnotationPropertyBlueprint> getProperties();
}
