package com.koch.ambeth.orm20;

/*-
 * #%L
 * jambeth-persistence-test
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

import java.util.List;

import com.koch.ambeth.persistence.api.IContextProvider;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.IDatabaseDisposeHook;
import com.koch.ambeth.persistence.api.IDatabaseMetaData;
import com.koch.ambeth.persistence.api.IDatabasePool;
import com.koch.ambeth.persistence.api.ILink;
import com.koch.ambeth.persistence.api.ISavepoint;
import com.koch.ambeth.persistence.api.ITable;
import com.koch.ambeth.util.state.IStateRollback;
import com.koch.ambeth.util.state.NoOpStateRollback;

public class DatabaseDummy implements IDatabase {
	@Override
	public void dispose() {
	}

	@Override
	public <T> T getAutowiredBeanInContext(Class<T> autowiredType) {
		return null;
	}

	@Override
	public <T> T getNamedBeanInContext(String beanName, Class<T> expectedType) {
		return null;
	}

	@Override
	public long getSessionId() {
		return 0;
	}

	@Override
	public void setSessionId(long sessionId) {
	}

	@Override
	public IContextProvider getContextProvider() {
		return null;
	}

	@Override
	public IDatabasePool getPool() {
		return null;
	}

	@Override
	public void flushAndRelease() {
	}

	@Override
	public void release(boolean errorOccured) {
	}

	@Override
	public void acquired(boolean readOnly) {
	}

	@Override
	public IDatabase getCurrent() {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public String[] getSchemaNames() {
		return null;
	}

	@Override
	public List<ITable> getTables() {
		return null;
	}

	@Override
	public List<ILink> getLinks() {
		return null;
	}

	@Override
	public ITable getTableByType(Class<?> entityType) {
		return null;
	}

	@Override
	public ITable getArchiveTableByType(Class<?> entityType) {
		return null;
	}

	@Override
	public ITable getTableByName(String tableName) {
		return null;
	}

	@Override
	public boolean test() {
		return false;
	}

	@Override
	public void flush() {
	}

	@Override
	public void revert() {
	}

	@Override
	public void revert(ISavepoint savepoint) {
	}

	@Override
	public ISavepoint setSavepoint() {
		return null;
	}

	@Override
	public void releaseSavepoint(ISavepoint savepoint) {
	}

	@Override
	public void rollback(ISavepoint savepoint) {
	}

	@Override
	public IStateRollback disableConstraints() {
		return NoOpStateRollback.instance;
	}

	@Override
	public List<ILink> getLinksByTables(ITable table1, ITable table2) {
		return null;
	}

	@Override
	public void registerDisposeHook(IDatabaseDisposeHook disposeHook) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unregisterDisposeHook(IDatabaseDisposeHook disposeHook) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isDisposed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IDatabaseMetaData getMetaData() {
		return null;
	}

	@Override
	public void registerNewTable(String tableName) {

	}
}
