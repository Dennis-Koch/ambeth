package com.koch.ambeth.revert;

import java.util.Collection;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.merge.util.setup.AbstractDatasetBuilder;
import com.koch.ambeth.merge.util.setup.IDatasetBuilder;
import com.koch.ambeth.merge.util.setup.IDatasetBuilderExtendable;
import com.koch.ambeth.persistence.xml.model.Address;
import com.koch.ambeth.persistence.xml.model.Employee;
import com.koch.ambeth.persistence.xml.model.Project;

public class RevertChangesTestFrameworkModule implements IInitializingModule {
	public static class DatasetBuilder extends AbstractDatasetBuilder {
		public Employee employee;

		@Override
		protected void buildDatasetInternal() {
			employee = createEntity(Employee.class);
			employee.setName("MyEmployee");

			Address address = createEntity(Address.class);
			address.setCity("DefaultCity");
			address.setStreet("DefaultStreet");
			employee.setPrimaryAddress(address);

			Project project = createEntity(Project.class);
			project.setName("MyProject");
			project.addEmployee(employee);
			employee.getAllProjects().add(project);
		}

		@Override
		public Collection<Class<? extends IDatasetBuilder>> getDependsOn() {
			return null;
		}
	}

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		IBeanConfiguration datasetBuilder = beanContextFactory.registerBean(DatasetBuilder.class)
				.autowireable(DatasetBuilder.class);
		beanContextFactory.link(datasetBuilder).to(IDatasetBuilderExtendable.class);
	}
}
