package de.osthus.ambeth.privilege.evaluation;

public interface IDeleteEntityStep
{
	IExecuteEntityStep allowDelete();

	IExecuteEntityStep skipDelete();

	IExecuteEntityStep denyDelete();
}
