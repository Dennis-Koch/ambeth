package de.osthus.ambeth.privilege.evaluation;

import de.osthus.ambeth.model.ISecurityScope;

public interface IPermissionEvaluation
{
	IScopedPermissionEvaluation scope(ISecurityScope scope);

	IPermissionEvaluationReadStep allowCreate();

	IPermissionEvaluationReadStep skipCreate();

	IPermissionEvaluationReadStep denyCreate();

	void allowEach();

	void skipEach();

	void denyEach();
}
