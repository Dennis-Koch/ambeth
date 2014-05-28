package de.osthus.ambeth.query.fulltext;

import org.junit.Test;

import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.SQLStructureList;
import de.osthus.ambeth.testutil.TestProperties;

@SQLStructureList({ @SQLStructure("/de/osthus/ambeth/query/Query_structure"), @SQLStructure("Catsearch_structure_context") })
public class CatsearchTest2 extends AbstractCatsearchTest
{

	@Test
	@TestProperties(name = "abc", value = "abc3")
	public void fulltextContext() throws Exception
	{
		fulltextDefault();
	}

}
