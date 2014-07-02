package de.osthus.ambeth.privilege.evaluation;

import de.osthus.ambeth.model.ISecurityScope;

public interface IEntityPermissionEvaluation
{
	IScopedEntityPermissionEvaluation scope(ISecurityScope scope);

	ICreateEntityStep allowRead();

	ICreateEntityPropertyStep allowReadProperty(String propertyName);

	void denyRead();

	IEntityPermissionEvaluation denyReadProperty(String propertyName);

	void allowEach();

	void skipEach();

	void denyEach();
}
