package com.koch.ambeth.merge.orm.blueprint;

import java.util.Collection;

public interface IEntityPropertyBlueprint
{
	public static final String NAME = "Name";
	public static final String TYPE = "Type";
	public static final String ORDER = "Order";
	public static final String READONLY = "Readonly";

	String getName();

	void setName(String name);

	String getType();

	void setType(String type);

	int getOrder();

	void setOrder(int order);

	boolean isReadonly();

	void setReadonly(boolean readonly);

	Collection<? extends IEntityAnnotationBlueprint> getAnnotations();
}
