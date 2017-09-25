package com.koch.ambeth.persistence.sql;

import java.util.Iterator;

/*-
 * #%L
 * jambeth-persistence
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

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.IList;

public class CompositeResultSet implements IInitializingBean, IResultSet, Iterator<Object[]> {
	protected IList<IResultSetProvider> resultSetProviderStack;
	protected IResultSet resultSet;

	protected Iterator<Object[]> resultSetIter;

	@Override
	public void afterPropertiesSet() {
		ParamChecker.assertNotNull(resultSetProviderStack, "ResultSetProviderStack");
	}

	@Override
	public void dispose() {
		if (resultSet != null) {
			resultSet.dispose();
			resultSet = null;
		}
		if (resultSetProviderStack != null) {
			for (IResultSetProvider resultSetProvider : resultSetProviderStack) {
				resultSetProvider.skipResultSet();
			}
			resultSetProviderStack = null;
		}
	}

	public List<IResultSetProvider> getResultSetProviderStack() {
		return resultSetProviderStack;
	}

	public void setResultSetProviderStack(IList<IResultSetProvider> resultSetProviderStack) {
		this.resultSetProviderStack = resultSetProviderStack;
	}

	protected IResultSet resolveNextResultSet() {
		IList<IResultSetProvider> resultSetProviderStack = this.resultSetProviderStack;
		if (resultSetProviderStack == null) {
			return null;
		}
		IResultSetProvider resultSetProvider = resultSetProviderStack.popLastElement();
		if (resultSetProvider == null) {
			this.resultSetProviderStack = null;
		}
		return resultSetProvider.getResultSet();
	}

	@Override
	public boolean hasNext() {
		while (true) {
			Iterator<Object[]> resultSetIter = this.resultSetIter;
			if (resultSetIter != null) {
				if (resultSetIter.hasNext()) {
					return true;
				}
				resultSet.dispose();
				resultSet = null;
				this.resultSetIter = null;
			}
			resultSet = resolveNextResultSet();
			if (resultSet == null) {
				return false;
			}
			this.resultSetIter = resultSet.iterator();
		}
	}

	@Override
	public Object[] next() {
		if (resultSetIter != null) {
			return resultSetIter.next();
		}
		return null;
	}

	@Override
	public Iterator<Object[]> iterator() {
		return this;
	}
}
