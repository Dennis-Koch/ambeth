package de.osthus.ambeth.privilege.evaluation;

public interface IPermissionEvaluationDeleteStep
{
	IPermissionEvaluationExecuteStep allowDelete();

	IPermissionEvaluationExecuteStep skipDelete();

	IPermissionEvaluationExecuteStep denyDelete();
}
