package de.osthus.ambeth.privilege.evaluation;

public interface IUpdateEntityStep
{
	IDeleteEntityStep allowUpdate();

	IDeleteEntityStep skipUpdate();

	IDeleteEntityStep denyUpdate();
}
