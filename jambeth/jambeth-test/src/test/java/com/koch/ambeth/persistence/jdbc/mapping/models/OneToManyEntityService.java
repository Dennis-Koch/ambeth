package com.koch.ambeth.persistence.jdbc.mapping.models;

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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.proxy.PersistenceContext;
import com.koch.ambeth.persistence.IServiceUtil;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.ITable;
import com.koch.ambeth.persistence.jdbc.mapping.IOneToManyEntityService;
import com.koch.ambeth.query.persistence.IVersionCursor;
import com.koch.ambeth.service.proxy.Service;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;

@Service(IOneToManyEntityService.class)
@PersistenceContext
public class OneToManyEntityService implements IOneToManyEntityService, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance(OneToManyEntityService.class)
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
	public OneToManyEntity getOneToManyEntityByName(String name)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<OneToManyEntity> getOneToManyEntitiesByNamesReturnCollection(String... names)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public List<OneToManyEntity> getOneToManyEntitiesByNamesReturnList(String... names)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<OneToManyEntity> getOneToManyEntitiesByNamesReturnSet(String... names)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public OneToManyEntity[] getOneToManyEntitiesByNamesReturnArray(String... names)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public OneToManyEntity getOneToManyEntityByNames(Collection<String> names)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public OneToManyEntity getOneToManyEntityByNames(List<String> names)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public OneToManyEntity getOneToManyEntityByNames(String... names)
	{
		throw new UnsupportedOperationException();
	}

	public void test(String name)
	{
		ITable aieTable = database.getTableByType(OneToManyEntity.class);
		ArrayList<String> names = new ArrayList<String>();
		names.add(name);
		IVersionCursor selectVersion = aieTable.selectVersion("Name", names);
		System.out.println(selectVersion);
	}

	@Override
	public void updateOneToManyEntity(OneToManyEntity oneToManyEntity)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteOneToManyEntity(OneToManyEntity oneToManyEntity)
	{
		throw new UnsupportedOperationException();
	}
}
