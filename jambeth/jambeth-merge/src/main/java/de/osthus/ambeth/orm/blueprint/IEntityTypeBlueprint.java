package de.osthus.ambeth.orm.blueprint;

import java.util.Collection;

public interface IEntityTypeBlueprint
{
	public static final String NAME = "Name";

	String getName();

	void setName(String name);

	Collection<String> getInherits();

	Collection<IEntityPropertyBlueprint> getProperties();

	Collection<IEntityAnnotationBlueprint> getAnnotations();
}
