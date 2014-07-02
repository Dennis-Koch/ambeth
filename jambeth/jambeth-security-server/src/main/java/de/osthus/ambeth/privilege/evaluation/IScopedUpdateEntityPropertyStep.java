package de.osthus.ambeth.privilege.evaluation;

public interface IScopedUpdateEntityPropertyStep
{
	IScopedDeleteEntityPropertyStep allowUpdateProperty();

	IScopedDeleteEntityPropertyStep skipUpdateProperty();

	IScopedDeleteEntityPropertyStep denyUpdateProperty();
}
