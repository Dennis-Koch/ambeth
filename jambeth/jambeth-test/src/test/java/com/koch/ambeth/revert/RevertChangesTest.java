package com.koch.ambeth.revert;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.merge.IMergeProcess;
import com.koch.ambeth.merge.IRevertChangesHelper;
import com.koch.ambeth.merge.IRevertChangesSavepoint;
import com.koch.ambeth.persistence.xml.model.Address;
import com.koch.ambeth.persistence.xml.model.Employee;
import com.koch.ambeth.persistence.xml.model.Project;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLData;
import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;

@TestPropertiesList({@TestProperties(name = ServiceConfigurationConstants.mappingFile,
		value = "com/koch/ambeth/persistence/xml/orm20.xml")})
@SQLData("/com/koch/ambeth/persistence/xml/Relations_data.sql")
@SQLStructure("/com/koch/ambeth/persistence/xml/Relations_structure.sql")
public class RevertChangesTest extends AbstractInformationBusWithPersistenceTest {
	@Autowired
	protected IEntityFactory entityFactory;

	@Autowired
	protected IMergeProcess mergeProcess;

	@Autowired
	protected IRevertChangesHelper revertChangesHelper;

	@Test
	public void simple() {
		Employee employee = entityFactory.createEntity(Employee.class);
		employee.setName("MyEmployee");

		Address address = entityFactory.createEntity(Address.class);
		address.setCity("DefaultCity");
		address.setStreet("DefaultStreet");
		employee.setPrimaryAddress(address);

		Project project = entityFactory.createEntity(Project.class);
		project.setName("MyProject");
		project.addEmployee(employee);
		employee.getAllProjects().add(project);
		mergeProcess.process(employee);

		IRevertChangesSavepoint sp = revertChangesHelper.createSavepoint(employee);
		Assert.assertEquals(7, sp.getSavedBusinessObjects().length);

		String initialName = employee.getName();
		employee.setName(initialName + "other");
		employee.setPrimaryAddress(null);
		employee.getAllProjects().clear();
		Assert.assertNotEquals(initialName, employee.getName());
		Assert.assertNull(employee.getPrimaryAddress());
		Assert.assertEquals(0, employee.getAllProjects().size());
		sp.revertChanges();
		Assert.assertEquals(initialName, employee.getName());
		Assert.assertNotNull(employee.getPrimaryAddress());
		Assert.assertEquals(1, employee.getAllProjects().size());
	}
}
