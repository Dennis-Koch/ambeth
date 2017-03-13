package com.koch.ambeth.security.server.privilege.evaluation;

public interface IScopedExecuteEntityStep
{
	void allowExecute();

	void skipExecute();

	void denyExecute();
}
