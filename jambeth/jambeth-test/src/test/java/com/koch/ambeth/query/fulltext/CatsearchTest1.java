package com.koch.ambeth.query.fulltext;

import org.junit.Test;

import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.testutil.SQLStructureList;

@SQLStructureList({ @SQLStructure("/com/koch/ambeth/query/Query_structure"), @SQLStructure("Catsearch_structure_ctxcat") })
public class CatsearchTest1 extends AbstractCatsearchTest
{

	@Test
	public void fulltextCtxCat() throws Exception
	{
		fulltextDefault();
	}

}
