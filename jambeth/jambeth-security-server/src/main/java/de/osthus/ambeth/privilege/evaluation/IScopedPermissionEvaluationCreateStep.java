package de.osthus.ambeth.privilege.evaluation;

public interface IScopedPermissionEvaluationCreateStep
{
	IScopedPermissionEvaluationUpdateStep allowCreate();

	IScopedPermissionEvaluationUpdateStep skipCreate();

	IScopedPermissionEvaluationUpdateStep denyCreate();
}
