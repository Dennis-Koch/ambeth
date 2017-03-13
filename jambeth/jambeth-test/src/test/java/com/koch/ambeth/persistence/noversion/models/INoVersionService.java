package com.koch.ambeth.persistence.noversion.models;

public interface INoVersionService
{
	NoVersion create(NoVersion entity);

	NoVersion update(NoVersion entity);

	void delete(NoVersion entity);
}
