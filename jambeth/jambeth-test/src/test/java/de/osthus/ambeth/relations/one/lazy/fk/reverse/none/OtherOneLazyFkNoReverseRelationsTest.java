package de.osthus.ambeth.relations.one.lazy.fk.reverse.none;

import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.testutil.TestProperties;

@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/relations/one/lazy/fk/reverse/none/other-orm.xml")
public class OtherOneLazyFkNoReverseRelationsTest extends OneLazyFkNoReverseRelationsTest
{
	@Override
	protected int getEntityA_Id()
	{
		return super.getEntityB_Id();
	}

	@Override
	protected int getEntityB_Id()
	{
		return super.getEntityA_Id();
	}
}
