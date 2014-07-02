package de.osthus.ambeth.privilege.evaluation;

public interface IScopedDeleteEntityPropertyStep
{
	void allowDeleteProperty();

	void skipDeleteProperty();

	void denyDeleteProperty();
}
