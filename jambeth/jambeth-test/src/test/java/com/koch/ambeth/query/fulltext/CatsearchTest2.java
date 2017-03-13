package com.koch.ambeth.query.fulltext;

import org.junit.Test;

import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.testutil.SQLStructureList;
import com.koch.ambeth.testutil.TestProperties;

@SQLStructureList({ @SQLStructure("/com/koch/ambeth/query/Query_structure"), @SQLStructure("Catsearch_structure_context") })
public class CatsearchTest2 extends AbstractCatsearchTest
{

	@Test
	@TestProperties(name = "abc", value = "abc3")
	public void fulltextContext() throws Exception
	{
		fulltextDefault();
	}

}
