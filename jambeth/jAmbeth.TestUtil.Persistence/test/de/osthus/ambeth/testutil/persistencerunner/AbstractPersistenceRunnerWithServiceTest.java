package de.osthus.ambeth.testutil.persistencerunner;

import junit.framework.Assert;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.model.IProjectService;
import de.osthus.ambeth.testutil.model.Project;
import de.osthus.ambeth.util.ParamChecker;

/**
 * Base class of the tests for the AmbethPersistenceRunner with the service to save data set.
 */
@TestModule(TestUtilServicesModule.class)
public abstract class AbstractPersistenceRunnerWithServiceTest extends AbstractPersistenceRunnerTest
{

	protected IProjectService projectService;

	public void setProjectService(IProjectService projectService)
	{
		this.projectService = projectService;
	}

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();
		ParamChecker.assertNotNull(projectService, "ProjectService");
	}

	/**
	 * Insert a new entity into the database with the given alternate key.
	 * 
	 * @param alternateKey
	 *            Alternate key to use
	 */
	void insertData(Integer alternateKey)
	{
		Assert.assertNotNull(alternateKey);
		Project project = new Project();
		project.setAlternateKey(alternateKey);
		project.setName("Project " + alternateKey);
		projectService.saveProject(project);
	}

}
