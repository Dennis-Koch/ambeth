package de.osthus.ambeth.training.travelguides.model;

public interface GuideBook extends GuideBookBaseEntity
{
	String NAME = "Name";

	String getName();

	void setName(String name);
}
