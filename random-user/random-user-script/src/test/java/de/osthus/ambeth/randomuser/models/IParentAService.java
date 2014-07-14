package de.osthus.ambeth.randomuser.models;

public interface IParentAService
{
	ParentA create(ParentA entity);

	ParentA retrieve(int id);

	ParentA update(ParentA entity);

	void delete(ParentA entity);
}
