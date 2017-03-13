package com.koch.ambeth.bytecode;

import com.koch.ambeth.merge.IEntityFactory;

/**
 * Base class for entities that have access to the {@link IEntityFactory}
 */
public abstract class AbstractEntity
{
	protected IEntityFactory entityFactory;

	protected AbstractEntity(IEntityFactory entityFactory)
	{
		this.entityFactory = entityFactory;
	}
}
