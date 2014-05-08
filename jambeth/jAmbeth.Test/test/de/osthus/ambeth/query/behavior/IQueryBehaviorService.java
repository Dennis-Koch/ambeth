package de.osthus.ambeth.query.behavior;

import de.osthus.ambeth.model.Material;

public interface IQueryBehaviorService
{
	Material getMaterialByName(String name);

	Material getMaterialByNameObjRefMode(String name);

	Material getMaterialByNameDefaultMode(String name);
}