package de.osthus.ambeth.privilege.evaluation;

public interface IScopedUpdateEntityStep
{
	IScopedDeleteEntityStep allowUpdate();

	IScopedDeleteEntityStep skipUpdate();

	IScopedDeleteEntityStep denyUpdate();
}
