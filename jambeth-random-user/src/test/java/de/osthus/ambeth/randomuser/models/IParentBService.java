package de.osthus.ambeth.randomuser.models;

public interface IParentBService
{
	ParentB create(ParentB entity);

	ParentB retrieve(int id);

	ParentB update(ParentB entity);

	void delete(ParentB entity);
}
