package de.osthus.ambeth.privilege.evaluation;

public interface IExecuteEntityStep
{
	void allowExecute();

	void skipExecute();

	void denyExecute();
}
