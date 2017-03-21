package com.koch.ambeth.query.sql;

/*-
 * #%L
 * jambeth-test
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.koch.ambeth.query.jdbc.sql.ITableAliasProvider;
import com.koch.ambeth.query.jdbc.sql.TableAliasProvider;
import com.koch.ambeth.testutil.AbstractIocTest;

public class TableAliasProviderTest extends AbstractIocTest {
	private ITableAliasProvider fixture;

	@Before
	public void setUp() throws Exception {
		fixture = beanContext.registerBean(TableAliasProvider.class).finish();
	}

	@Test
	public void testGetNextJoinAlias() {
		String joinAlias = fixture.getNextJoinAlias();
		assertEquals("J_A", joinAlias);
	}

	@Test
	public void testGetNextSubQueryAlias() {
		String joinAlias = fixture.getNextSubQueryAlias();
		assertEquals("S_A", joinAlias);
	}

	@Test
	public void testMixedAliases() {
		String joinAlias;
		joinAlias = fixture.getNextSubQueryAlias();
		assertEquals("S_A", joinAlias);

		joinAlias = fixture.getNextJoinAlias();
		assertEquals("J_A", joinAlias);

		joinAlias = fixture.getNextJoinAlias();
		assertEquals("J_B", joinAlias);

		joinAlias = fixture.getNextSubQueryAlias();
		assertEquals("S_B", joinAlias);

		joinAlias = fixture.getNextJoinAlias();
		assertEquals("J_C", joinAlias);

		joinAlias = fixture.getNextSubQueryAlias();
		assertEquals("S_C", joinAlias);
	}

	@Test
	public void testMultiCharAlias() {
		// Iterating to the next interesting cases
		for (int i = 0; i < 25; i++) {
			fixture.getNextJoinAlias();
		}
		String joinAlias = fixture.getNextJoinAlias();
		assertEquals("J_Z", joinAlias);

		joinAlias = fixture.getNextJoinAlias();
		assertEquals("J_AA", joinAlias);

		joinAlias = fixture.getNextJoinAlias();
		assertEquals("J_AB", joinAlias);

		// Iterating to the next interesting cases
		for (int i = 0; i < 23; i++) {
			fixture.getNextJoinAlias();
		}
		joinAlias = fixture.getNextJoinAlias();
		assertEquals("J_AZ", joinAlias);

		joinAlias = fixture.getNextJoinAlias();
		assertEquals("J_BA", joinAlias);

		// Iterating to the next interesting cases
		for (int i = 0; i < ((2 * 26 - 1) * 26 - 2); i++) {
			fixture.getNextJoinAlias();
		}
		joinAlias = fixture.getNextJoinAlias();
		assertEquals("J_AZZ", joinAlias);

		joinAlias = fixture.getNextJoinAlias();
		assertEquals("J_BAA", joinAlias);

		joinAlias = fixture.getNextJoinAlias();
		assertEquals("J_BAB", joinAlias);
	}
}
