package com.koch.ambeth.persistence.xml.model;

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

import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.proxy.PersistenceContext;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.service.proxy.Service;

@Service(IProjectService.class)
@PersistenceContext
public class ProjectService implements IProjectService, IStartingBean {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IQueryBuilderFactory queryBuilderFactory;

	protected IQuery<Project> queryProjectAll;

	protected IQuery<Project> queryProjectByName;

	@Override
	public void afterStarted() throws Throwable {
		{
			queryProjectAll = queryBuilderFactory.create(Project.class).build();
		}
		{
			IQueryBuilder<Project> qb = queryBuilderFactory.create(Project.class);
			queryProjectByName =
					qb.build(qb.isEqualTo(qb.property(Project.Name), qb.valueName(Project.Name)));
		}
	}

	@Override
	public List<Project> getAllProjects() {
		return queryProjectAll.retrieve();
	}

	@Override
	public Project getProjectByName(String name) {
		return queryProjectByName.param(Project.Name, name).retrieveSingle();
	}

	@Override
	public void saveProject(Project project) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteProject(Project project) {
		throw new UnsupportedOperationException();
	}
}
