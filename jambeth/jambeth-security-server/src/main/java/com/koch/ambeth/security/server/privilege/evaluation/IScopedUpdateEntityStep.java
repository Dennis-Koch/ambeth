package com.koch.ambeth.security.server.privilege.evaluation;

public interface IScopedUpdateEntityStep
{
	IScopedDeleteEntityStep allowUpdate();

	IScopedDeleteEntityStep skipUpdate();

	IScopedDeleteEntityStep denyUpdate();
}
