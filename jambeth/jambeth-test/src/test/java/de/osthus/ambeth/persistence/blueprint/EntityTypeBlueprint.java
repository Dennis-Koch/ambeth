package de.osthus.ambeth.persistence.blueprint;

import java.util.Collection;

import de.osthus.ambeth.model.IAbstractEntity;
import de.osthus.ambeth.orm.blueprint.IEntityTypeBlueprint;

public interface EntityTypeBlueprint extends IAbstractEntity, IEntityTypeBlueprint
{
	@Override
	Collection<EntityPropertyBlueprint> getProperties();

	@Override
	Collection<EntityAnnotationBlueprint> getAnnotations();
}
