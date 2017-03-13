package com.koch.ambeth.security.server.privilege.evaluation;

public interface IScopedCreateEntityStep
{
	IScopedUpdateEntityStep allowCreate();

	IScopedUpdateEntityStep skipCreate();

	IScopedUpdateEntityStep denyCreate();
}
