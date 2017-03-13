package com.koch.ambeth.security.server.privilege.evaluation;

public interface IDeleteEntityStep
{
	IExecuteEntityStep allowDelete();

	IExecuteEntityStep skipDelete();

	IExecuteEntityStep denyDelete();
}
