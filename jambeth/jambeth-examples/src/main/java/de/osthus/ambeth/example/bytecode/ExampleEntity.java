package de.osthus.ambeth.example.bytecode;

import java.util.Collection;

import de.osthus.ambeth.annotation.EntityEqualsAspect;

@EntityEqualsAspect
public interface ExampleEntity
{
	int getId();

	int getVersion();

	String getCreatedBy();

	String getUpdatedBy();

	long getCreatedOn();

	long getUpdatedOn();

	String getName();

	void setName(String name);

	Collection<ExampleEntity> getOtherEntities();
}