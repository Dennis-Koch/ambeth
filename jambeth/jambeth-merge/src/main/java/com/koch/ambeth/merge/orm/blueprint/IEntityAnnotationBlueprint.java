package com.koch.ambeth.merge.orm.blueprint;

import java.util.Collection;

public interface IEntityAnnotationBlueprint
{
	public static final String TYPE = "Type";

	String getType();

	void setType(String type);

	Collection<? extends IEntityAnnotationPropertyBlueprint> getProperties();
}
