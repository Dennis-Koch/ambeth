package com.koch.ambeth.persistence.jdbc.compositeid.models;

public interface ICompositeIdEntityService
{
	CompositeIdEntity create(CompositeIdEntity entity);

	CompositeIdEntity update(CompositeIdEntity entity);

	void delete(CompositeIdEntity entity);
}
