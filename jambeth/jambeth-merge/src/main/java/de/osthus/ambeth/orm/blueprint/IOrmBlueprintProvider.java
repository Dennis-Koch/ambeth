package de.osthus.ambeth.orm.blueprint;


public interface IOrmBlueprintProvider
{
	IEntityTypeBlueprint resolveEntityTypeBlueprint(String entityTypeName);
}
