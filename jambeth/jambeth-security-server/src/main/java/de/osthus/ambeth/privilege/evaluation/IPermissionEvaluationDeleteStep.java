package de.osthus.ambeth.privilege.evaluation;

public interface IPermissionEvaluationDeleteStep
{
	void allowDelete();

	void skipDelete();

	void denyDelete();
}
