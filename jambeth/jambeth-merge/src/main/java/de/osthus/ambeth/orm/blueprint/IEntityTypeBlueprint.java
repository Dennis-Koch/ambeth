package de.osthus.ambeth.orm.blueprint;

import java.util.Collection;

public interface IEntityTypeBlueprint
{
	public static final String NAME = "Name";

	String getName();

	void setName(String name);

	boolean isClass();

	void setIsClass(boolean isClass);

	String getSuperclass();

	void setSuperclass(String superclass);

	Collection<String> getInterfaces();

	Collection<IEntityPropertyBlueprint> getProperties();

	Collection<IEntityAnnotationBlueprint> getAnnotations();

}
