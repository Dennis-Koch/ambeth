package com.koch.ambeth.persistence.xml.model;

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
public class ProjectService implements IProjectService, IStartingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IQueryBuilderFactory queryBuilderFactory;

	protected IQuery<Project> queryProjectAll;

	protected IQuery<Project> queryProjectByName;

	@Override
	public void afterStarted() throws Throwable
	{
		{
			queryProjectAll = queryBuilderFactory.create(Project.class).build();
		}
		{
			IQueryBuilder<Project> qb = queryBuilderFactory.create(Project.class);
			queryProjectByName = qb.build(qb.isEqualTo(qb.property(Project.Name), qb.valueName(Project.Name)));
		}
	}

	@Override
	public List<Project> getAllProjects()
	{
		return queryProjectAll.retrieve();
	}

	@Override
	public Project getProjectByName(String name)
	{
		return queryProjectByName.param(Project.Name, name).retrieveSingle();
	}

	@Override
	public void saveProject(Project project)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteProject(Project project)
	{
		throw new UnsupportedOperationException();
	}
}
