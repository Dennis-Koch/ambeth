package com.koch.ambeth.example.bytecode;

import java.util.Collection;

import com.koch.ambeth.util.annotation.EntityEqualsAspect;

@EntityEqualsAspect
public interface ExampleEntity {
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