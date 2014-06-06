package de.osthus.ambeth.privilege.evaluation;

public interface IScopedPermissionEvaluationDeleteStep
{
	IPermissionEvaluationResult allowDelete();

	IPermissionEvaluationResult skipDelete();

	IPermissionEvaluationResult denyDelete();
}
