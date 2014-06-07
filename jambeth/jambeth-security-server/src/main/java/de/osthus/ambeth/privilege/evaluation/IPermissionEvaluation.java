package de.osthus.ambeth.privilege.evaluation;

import de.osthus.ambeth.model.ISecurityScope;

public interface IPermissionEvaluation
{
	IScopedPermissionEvaluation scope(ISecurityScope scope);

	IPermissionEvaluationCreateStep allowRead();

	IPermissionEvaluationResult denyRead();

	IPermissionEvaluationResult allowEach();

	IPermissionEvaluationResult skipEach();

	IPermissionEvaluationResult denyEach();
}
