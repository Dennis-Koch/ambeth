package de.osthus.ambeth.privilege.evaluation;

public interface IUpdateEntityPropertyStep
{
	IDeleteEntityPropertyStep allowUpdateProperty();

	IDeleteEntityPropertyStep skipUpdateProperty();

	IDeleteEntityPropertyStep denyUpdateProperty();
}
