package de.osthus.ambeth.privilege.evaluation;

public interface IPermissionEvaluationDeleteStep
{
	IPermissionEvaluationResult allowDelete();

	IPermissionEvaluationResult skipDelete();

	IPermissionEvaluationResult denyDelete();
}
