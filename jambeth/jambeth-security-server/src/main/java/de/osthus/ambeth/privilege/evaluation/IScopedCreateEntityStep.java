package de.osthus.ambeth.privilege.evaluation;

public interface IScopedCreateEntityStep
{
	IScopedUpdateEntityStep allowCreate();

	IScopedUpdateEntityStep skipCreate();

	IScopedUpdateEntityStep denyCreate();
}
