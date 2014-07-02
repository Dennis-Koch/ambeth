package de.osthus.ambeth.privilege.evaluation;

public interface IScopedEntityPermissionEvaluation
{
	IScopedCreateEntityStep allowRead();

	IScopedCreateEntityPropertyStep allowReadProperty(String propertyName);

	void denyRead();

	IScopedEntityPermissionEvaluation denyReadProperty(String propertyName);

	void allowEach();

	void skipEach();

	void denyEach();
}
