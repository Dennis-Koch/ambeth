package de.osthus.ambeth.privilege.evaluation;

public interface IScopedPermissionEvaluation
{
	IScopedPermissionEvaluationCreateStep allowRead();

	IPermissionEvaluationResult denyRead();

	IPermissionEvaluationResult allowEach();

	IPermissionEvaluationResult skipEach();

	IPermissionEvaluationResult denyEach();
}
