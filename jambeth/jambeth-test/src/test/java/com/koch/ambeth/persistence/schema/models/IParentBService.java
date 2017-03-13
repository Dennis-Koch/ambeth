package com.koch.ambeth.persistence.schema.models;

public interface IParentBService
{
	ParentB create(ParentB entity);

	ParentB retrieve(int id);

	ParentB update(ParentB entity);

	void delete(ParentB entity);
}
