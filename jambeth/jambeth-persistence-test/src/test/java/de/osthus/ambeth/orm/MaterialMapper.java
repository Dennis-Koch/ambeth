package de.osthus.ambeth.orm;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.model.Material;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.ITable;

public class MaterialMapper extends DefaultDatabaseMapper
{
	@SuppressWarnings("unused")
	@LogInstance(MaterialMapper.class)
	private ILogger log;

	@Override
	public void mapFields(IDatabase database)
	{
		ITable table = database.mapTable("MATERIAL", Material.class);
		mapIdAndVersion(table);
		table.mapField("NAME", "Name");
	}
}
