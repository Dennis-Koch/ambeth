package de.osthus.ambeth.persistence.blueprint;

import java.util.Collection;

import de.osthus.ambeth.model.IAbstractEntity;
import de.osthus.ambeth.orm.blueprint.IEntityPropertyBlueprint;

public interface EntityPropertyBlueprint extends IAbstractEntity, IEntityPropertyBlueprint
{
	@Override
	Collection<EntityAnnotationBlueprint> getAnnotations();
}
