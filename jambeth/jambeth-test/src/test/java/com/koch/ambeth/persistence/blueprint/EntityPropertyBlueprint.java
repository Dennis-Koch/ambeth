package com.koch.ambeth.persistence.blueprint;

import java.util.Collection;

import com.koch.ambeth.merge.orm.blueprint.IEntityPropertyBlueprint;
import com.koch.ambeth.model.IAbstractEntity;

public interface EntityPropertyBlueprint extends IAbstractEntity, IEntityPropertyBlueprint
{
	@Override
	Collection<EntityAnnotationBlueprint> getAnnotations();
}
