package de.osthus.ambeth.privilege.evaluation;

public interface IScopedPermissionEvaluationDeleteStep
{
	void allowDelete();

	void skipDelete();

	void denyDelete();
}
