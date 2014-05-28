package de.osthus.ambeth.persistence.noversion.models;

public interface INoVersionService
{
	NoVersion create(NoVersion entity);

	NoVersion update(NoVersion entity);

	void delete(NoVersion entity);
}
