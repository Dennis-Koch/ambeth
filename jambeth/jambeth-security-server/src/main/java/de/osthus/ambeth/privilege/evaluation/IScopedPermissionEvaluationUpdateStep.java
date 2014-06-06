package de.osthus.ambeth.privilege.evaluation;

public interface IScopedPermissionEvaluationUpdateStep
{
	IScopedPermissionEvaluationDeleteStep allowUpdate();

	IScopedPermissionEvaluationDeleteStep skipUpdate();

	IScopedPermissionEvaluationDeleteStep denyUpdate();
}
