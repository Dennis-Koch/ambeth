package de.osthus.ambeth.privilege.evaluation;

public interface IScopedPermissionEvaluation
{
	IScopedPermissionEvaluationReadStep allowCreate();

	IScopedPermissionEvaluationReadStep skipCreate();

	IScopedPermissionEvaluationReadStep denyCreate();

	void allowEach();

	void skipEach();

	void denyEach();
}
