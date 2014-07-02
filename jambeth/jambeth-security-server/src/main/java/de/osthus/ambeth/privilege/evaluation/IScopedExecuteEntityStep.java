package de.osthus.ambeth.privilege.evaluation;

public interface IScopedExecuteEntityStep
{
	void allowExecute();

	void skipExecute();

	void denyExecute();
}
