package de.osthus.ambeth.orm;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.model.Unit;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.ITable;

public class UnitMapper extends DefaultDatabaseMapper
{
	@SuppressWarnings("unused")
	@LogInstance(UnitMapper.class)
	private ILogger log;

	@Override
	public void mapFields(IDatabase database)
	{
		ITable table = database.mapTable("UNIT", Unit.class);
		mapIdAndVersion(table);
		table.mapField("NAME", "Name");
	}
}
