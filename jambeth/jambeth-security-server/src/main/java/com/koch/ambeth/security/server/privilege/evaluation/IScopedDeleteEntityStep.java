package com.koch.ambeth.security.server.privilege.evaluation;

public interface IScopedDeleteEntityStep
{
	IScopedExecuteEntityStep allowDelete();

	IScopedExecuteEntityStep skipDelete();

	IScopedExecuteEntityStep denyDelete();
}
