package de.osthus.ambeth.privilege.evaluation;

public interface IScopedPermissionEvaluationDeleteStep
{
	IScopedPermissionEvaluationExecuteStep allowDelete();

	IScopedPermissionEvaluationExecuteStep skipDelete();

	IScopedPermissionEvaluationExecuteStep denyDelete();
}
