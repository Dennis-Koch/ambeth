package com.koch.ambeth.persistence.jdbc.array;

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

import java.util.List;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.proxy.PersistenceContext;
import com.koch.ambeth.persistence.IServiceUtil;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.ITable;
import com.koch.ambeth.service.proxy.Service;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;

@Service(IArrayObjectService.class)
@PersistenceContext
public class ArrayObjectService implements IArrayObjectService, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance(ArrayObjectService.class)
	private ILogger log;

	protected IDatabase database;

	protected IServiceUtil serviceUtil;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(database, "database");
		ParamChecker.assertNotNull(serviceUtil, "serviceUtil");
	}

	public void setDatabase(IDatabase database)
	{
		this.database = database;
	}

	public void setServiceUtil(IServiceUtil serviceUtil)
	{
		this.serviceUtil = serviceUtil;
	}

	@Override
	public List<ArrayObject> getAllArrayObjects()
	{
		ITable table = database.getTableByType(ArrayObject.class);

		ArrayList<ArrayObject> list = new ArrayList<ArrayObject>();
		serviceUtil.loadObjectsIntoCollection(list, ArrayObject.class, table.selectAll());
		return list;
	}

	@Override
	public ArrayObject getArrayObject(Integer id)
	{
		ITable table = database.getTableByType(ArrayObject.class);

		ArrayList<ArrayObject> list = new ArrayList<ArrayObject>(1);
		List<Object> ids = new ArrayList<Object>();
		ids.add(id);
		serviceUtil.loadObjectsIntoCollection(list, ArrayObject.class, table.selectVersion(ids));
		if (list.size() > 0)
		{
			return list.get(0);
		}
		return null;
	}

	@Override
	public List<ArrayObject> getArrayObjects(Integer... id)
	{
		ITable table = database.getTableByType(ArrayObject.class);

		ArrayList<ArrayObject> list = new ArrayList<ArrayObject>(id.length);
		serviceUtil.loadObjectsIntoCollection(list, ArrayObject.class, table.selectVersion(new ArrayList<Object>(id)));
		return list;
	}

	@Override
	public void updateArrayObject(ArrayObject arrayObject)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteArrayObject(ArrayObject arrayObject)
	{
		throw new UnsupportedOperationException();
	}
}
