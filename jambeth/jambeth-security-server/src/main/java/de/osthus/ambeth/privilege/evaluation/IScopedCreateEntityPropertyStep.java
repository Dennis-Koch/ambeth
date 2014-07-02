package de.osthus.ambeth.privilege.evaluation;

public interface IScopedCreateEntityPropertyStep
{
	IScopedUpdateEntityPropertyStep allowCreateProperty();

	void allowCUDProperty();

	IScopedUpdateEntityPropertyStep skipCreateProperty();

	void skipCUDProperty();

	IScopedUpdateEntityPropertyStep denyCreateProperty();

	void denyCUDProperty();
}
