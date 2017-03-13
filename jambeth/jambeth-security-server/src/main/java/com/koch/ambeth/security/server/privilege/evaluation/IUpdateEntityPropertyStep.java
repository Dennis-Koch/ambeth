package com.koch.ambeth.security.server.privilege.evaluation;

public interface IUpdateEntityPropertyStep
{
	IDeleteEntityPropertyStep allowUpdateProperty();

	IDeleteEntityPropertyStep skipUpdateProperty();

	IDeleteEntityPropertyStep denyUpdateProperty();
}
