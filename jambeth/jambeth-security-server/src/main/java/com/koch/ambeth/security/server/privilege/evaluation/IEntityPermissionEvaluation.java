package com.koch.ambeth.security.server.privilege.evaluation;

import com.koch.ambeth.service.model.ISecurityScope;

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
