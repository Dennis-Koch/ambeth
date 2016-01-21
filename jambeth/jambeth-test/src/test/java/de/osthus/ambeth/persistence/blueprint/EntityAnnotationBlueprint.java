package de.osthus.ambeth.persistence.blueprint;

import java.util.Collection;

import de.osthus.ambeth.model.IAbstractEntity;
import de.osthus.ambeth.orm.blueprint.IEntityAnnotationBlueprint;

public interface EntityAnnotationBlueprint extends IAbstractEntity, IEntityAnnotationBlueprint
{
	@Override
	Collection<EntityAnnotationPropertyBlueprint> getProperties();
}
