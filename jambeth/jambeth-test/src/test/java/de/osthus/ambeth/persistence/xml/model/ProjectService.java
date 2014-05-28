package de.osthus.ambeth.persistence.xml.model;

import java.util.ArrayList;
import java.util.List;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IField;
import de.osthus.ambeth.persistence.IServiceUtil;
import de.osthus.ambeth.proxy.PersistenceContext;
import de.osthus.ambeth.proxy.Service;
import de.osthus.ambeth.util.ParamChecker;

@Service(IProjectService.class)
@PersistenceContext
public class ProjectService implements IInitializingBean, IProjectService
{
	@SuppressWarnings("unused")
	@LogInstance(ProjectService.class)
	private ILogger log;

	protected IDatabase database;

	protected IServiceUtil serviceUtil;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(serviceUtil, "serviceUtil");
		ParamChecker.assertNotNull(database, "database");
	}

	public void setServiceUtil(IServiceUtil serviceUtil)
	{
		this.serviceUtil = serviceUtil;
	}

	public void setDatabase(IDatabase database)
	{
		this.database = database;
	}

	@Override
	public List<Project> getAllProjects()
	{
		List<Project> projects = new ArrayList<Project>();
		serviceUtil.loadObjectsIntoCollection(projects, Project.class, database.getTableByType(Project.class).selectAll());
		return projects;
	}

	@Override
	public Project getProjectByName(String name)
	{
		IField nameField = database.getTableByType(Project.class).getFieldByMemberName("Name");
		return serviceUtil.loadObject(Project.class, nameField.findSingle(name));
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
