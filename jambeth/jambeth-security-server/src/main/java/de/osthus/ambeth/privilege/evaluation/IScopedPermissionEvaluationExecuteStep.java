package de.osthus.ambeth.privilege.evaluation;

public interface IScopedPermissionEvaluationExecuteStep
{
	IPermissionEvaluationResult allowExecute();

	IPermissionEvaluationResult skipExecute();

	IPermissionEvaluationResult denyExecute();
}
