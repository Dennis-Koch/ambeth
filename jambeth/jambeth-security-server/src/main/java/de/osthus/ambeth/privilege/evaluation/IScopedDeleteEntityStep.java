package de.osthus.ambeth.privilege.evaluation;

public interface IScopedDeleteEntityStep
{
	IScopedExecuteEntityStep allowDelete();

	IScopedExecuteEntityStep skipDelete();

	IScopedExecuteEntityStep denyDelete();
}
