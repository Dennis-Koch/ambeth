package com.koch.ambeth.security.server.privilege.evaluation;

public interface IUpdateEntityStep
{
	IDeleteEntityStep allowUpdate();

	IDeleteEntityStep skipUpdate();

	IDeleteEntityStep denyUpdate();
}
