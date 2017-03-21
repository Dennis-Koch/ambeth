package com.koch.ambeth.query.jdbc.sql;

/*-
 * #%L
 * jambeth-query-jdbc
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

import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

/**
 * Provides TableAliases to a Query and its SubQueries.
 *
 * NOT thread-save! Each Query (not SubQuery) needs its own instance.
 */
public class TableAliasProvider implements ITableAliasProvider, IInitializingBean, IDisposableBean {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	private static final int lettersInTheAlphabet = 26;

	private static final char firstTableAlias = 'A';

	protected IThreadLocalObjectCollector objectCollector;

	protected StringBuilder sb;

	private int nextJoinIndex = 0;

	private int nextSubQueryIndex = 0;

	@Override
	public void afterPropertiesSet() throws Throwable {
		ParamChecker.assertNotNull(objectCollector, "objectCollector");

		sb = objectCollector.create(StringBuilder.class);
	}

	@Override
	public void destroy() throws Throwable {
		objectCollector.dispose(sb);
	}

	public void setObjectCollector(IThreadLocalObjectCollector objectCollector) {
		this.objectCollector = objectCollector;
	}

	@Override
	public String getNextJoinAlias() {
		return getNextAlias("J_", nextJoinIndex++);
	}

	@Override
	public String getNextSubQueryAlias() {
		return getNextAlias("S_", nextSubQueryIndex++);
	}

	private String getNextAlias(String prefix, int index) {
		try {
			sb.append(prefix);
			appendNextAlias(index);
			return sb.toString();
		}
		finally {
			sb.setLength(0);
		}
	}

	private void appendNextAlias(int index) {
		int mine = index % lettersInTheAlphabet;
		int others = index / lettersInTheAlphabet;

		if (others > 0) {
			appendNextAlias(others - 1);
		}
		sb.append((char) (firstTableAlias + mine));
	}
}
