package de.osthus.ambeth.privilege.evaluation;

public interface IScopedPermissionEvaluation
{
	IScopedPermissionEvaluationReadStep allowCreate();

	IScopedPermissionEvaluationReadStep skipCreate();

	IScopedPermissionEvaluationReadStep denyCreate();

	IPermissionEvaluationResult allowEach();

	IPermissionEvaluationResult skipEach();

	IPermissionEvaluationResult denyEach();
}
