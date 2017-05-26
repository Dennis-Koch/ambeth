package com.koch.ambeth.revert;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.informationbus.persistence.setup.SQLData;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.IRevertChangesHelper;
import com.koch.ambeth.merge.IRevertChangesSavepoint;
import com.koch.ambeth.persistence.xml.model.Employee;
import com.koch.ambeth.persistence.xml.model.Project;
import com.koch.ambeth.revert.RevertChangesTestFrameworkModule.DatasetBuilder;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestFrameworkModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;

@TestPropertiesList({
		@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "com/koch/ambeth/persistence/xml/orm20.xml")})
@TestFrameworkModule(RevertChangesTestFrameworkModule.class)
@SQLData("/com/koch/ambeth/persistence/xml/Relations_data.sql")
@SQLStructure("/com/koch/ambeth/persistence/xml/Relations_structure.sql")
public class RevertChangesTest extends AbstractInformationBusWithPersistenceTest {
	@Autowired
	protected DatasetBuilder datasetBuilder;

	@Autowired
	protected IRevertChangesHelper revertChangesHelper;

	@Test
	public void createSavepoint() {
		Employee employee = datasetBuilder.employee;

		// it is just that simple. create a Savepoint of the given instance of the employee
		IRevertChangesSavepoint sp = revertChangesHelper.createSavepoint(employee);
		Assert.assertEquals(7, sp.getSavedBusinessObjects().length);

		String initialName = employee.getName();
		employee.setName(initialName + "other");
		employee.setPrimaryAddress(null);
		employee.getAllProjects().clear();

		Assert.assertNotEquals(initialName, employee.getName());
		Assert.assertNull(employee.getPrimaryAddress());
		Assert.assertEquals(0, employee.getAllProjects().size());

		// recover the previous Savepoint. A Savepoint knows the tracked entities and their
		// captured initial state
		sp.revertChanges();

		Assert.assertEquals(initialName, employee.getName());
		Assert.assertNotNull(employee.getPrimaryAddress());
		Assert.assertEquals(1, employee.getAllProjects().size());
	}

	@Test
	public void revertChanges() {
		Employee employee = datasetBuilder.employee;

		// modify entity
		String initialName = employee.getName();
		employee.setName(initialName + "other");
		employee.setPrimaryAddress(null);
		employee.getAllProjects().clear();

		// ensure modification is done
		Assert.assertNotEquals(initialName, employee.getName());
		Assert.assertNull(employee.getPrimaryAddress());
		Assert.assertEquals(0, employee.getAllProjects().size());

		revertChangesHelper.revertChanges(employee);

		// ensure initial step is recovered
		Assert.assertEquals(initialName, employee.getName());
		Assert.assertNotNull(employee.getPrimaryAddress());
		Assert.assertEquals(1, employee.getAllProjects().size());

		// now test the optional transitivity flag for reverting changes on the project from the
		// point-of-view of the employee
		Project project = employee.getAllProjects().iterator().next();
		String initialProjectName = project.getName();
		project.setName(initialProjectName + "other");
		Assert.assertNotEquals(initialProjectName, project.getName());
		revertChangesHelper.revertChanges(employee);
		Assert.assertNotEquals(initialProjectName, project.getName());
		revertChangesHelper.revertChanges(employee, true);
		Assert.assertEquals(initialProjectName, project.getName());
	}
}
