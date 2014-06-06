package de.osthus.ambeth.privilege.evaluation;

public interface IScopedPermissionEvaluationReadStep
{
	IScopedPermissionEvaluationUpdateStep allowRead();

	IScopedPermissionEvaluationUpdateStep skipRead();

	IScopedPermissionEvaluationUpdateStep denyRead();
}
