package com.koch.ambeth.security.server.privilege.evaluation;

public interface IScopedUpdateEntityPropertyStep
{
	IScopedDeleteEntityPropertyStep allowUpdateProperty();

	IScopedDeleteEntityPropertyStep skipUpdateProperty();

	IScopedDeleteEntityPropertyStep denyUpdateProperty();
}
