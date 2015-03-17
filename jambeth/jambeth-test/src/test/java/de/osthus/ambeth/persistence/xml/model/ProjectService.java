package de.osthus.ambeth.persistence.xml.model;

import java.util.List;

import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.PersistenceContext;
import de.osthus.ambeth.proxy.Service;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.query.IQueryBuilderFactory;

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
