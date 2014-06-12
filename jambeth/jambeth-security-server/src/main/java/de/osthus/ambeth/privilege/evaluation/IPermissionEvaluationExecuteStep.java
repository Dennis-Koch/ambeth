package de.osthus.ambeth.privilege.evaluation;

public interface IPermissionEvaluationExecuteStep
{
	IPermissionEvaluationResult allowExecute();

	IPermissionEvaluationResult skipExecute();

	IPermissionEvaluationResult denyExecute();
}
