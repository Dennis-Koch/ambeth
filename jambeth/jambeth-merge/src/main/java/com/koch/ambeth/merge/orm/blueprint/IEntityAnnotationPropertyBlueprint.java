package com.koch.ambeth.merge.orm.blueprint;

public interface IEntityAnnotationPropertyBlueprint
{
	public static final String NAME = "Name";
	public static final String VALUE = "Value";

	String getName();

	void setName(String name);

	String getValue();

	void setValue(String value);
}
