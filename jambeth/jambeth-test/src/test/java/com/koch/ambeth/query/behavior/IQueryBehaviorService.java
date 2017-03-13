package com.koch.ambeth.query.behavior;

import com.koch.ambeth.model.Material;

public interface IQueryBehaviorService
{
	Material getMaterialByName(String name);

	Material getMaterialByNameObjRefMode(String name);

	Material getMaterialByNameDefaultMode(String name);
}