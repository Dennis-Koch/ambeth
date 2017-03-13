package com.koch.ambeth;

import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.model.Material;
import com.koch.ambeth.model.Unit;

public class ObjectMother
{
	public static Material getNewMaterial(IEntityFactory entityFactory, Integer id, Integer version, String name)
	{
		Material material = entityFactory.createEntity(Material.class);
		if (id != null)
		{
			material.setId(id);
		}
		if (version != null)
		{
			material.setVersion(version.shortValue());
		}
		material.setName(name);
		return material;
	}

	public static Unit getNewUnit(IEntityFactory entityFactory, Integer id, Integer version, String name)
	{
		Unit unit = entityFactory.createEntity(Unit.class);
		if (id != null)
		{
			unit.setId(id);
		}
		if (version != null)
		{
			unit.setVersion(version.shortValue());
		}
		unit.setName(name);
		return unit;
	}
}
