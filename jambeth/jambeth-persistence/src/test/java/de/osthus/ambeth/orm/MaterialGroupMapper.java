package de.osthus.ambeth.orm;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.model.MaterialGroup;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.ITable;

public class MaterialGroupMapper extends DefaultDatabaseMapper
{
	@SuppressWarnings("unused")
	@LogInstance(MaterialGroupMapper.class)
	private ILogger log;

	@Override
	public void mapFields(IDatabase database)
	{
		ITable table = database.mapTable("MATERIAL_GROUP", MaterialGroup.class);
		mapIdAndVersion(table);
		table.mapField("NAME", "Name");
	}
}
