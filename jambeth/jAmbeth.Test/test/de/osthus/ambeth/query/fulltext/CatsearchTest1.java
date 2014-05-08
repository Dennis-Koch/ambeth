package de.osthus.ambeth.query.fulltext;

import org.junit.Test;

import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.SQLStructureList;

@SQLStructureList({ @SQLStructure("/de/osthus/ambeth/query/Query_structure"), @SQLStructure("Catsearch_structure_ctxcat") })
public class CatsearchTest1 extends AbstractCatsearchTest
{

	@Test
	public void fulltextCtxCat() throws Exception
	{
		fulltextDefault();
	}

}
