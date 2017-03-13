package com.koch.ambeth.security.server.privilege.evaluation;

public interface IExecuteEntityStep
{
	void allowExecute();

	void skipExecute();

	void denyExecute();
}
