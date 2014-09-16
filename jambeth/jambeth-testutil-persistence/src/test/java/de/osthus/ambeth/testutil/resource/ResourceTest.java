package de.osthus.ambeth.testutil.resource;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestProperties;

@SQLStructure("resource_structure.sql")
public class ResourceTest extends AbstractPersistenceTest
{
	@Property(name = "resource.properties.loaded", defaultValue = "false")
	private boolean resourcePropertiesLoaded;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Test
	@TestProperties(file = "resource.properties")
	public void testProperties()
	{
		assertTrue(resourcePropertiesLoaded);
	}

	@Test
	@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "resource_orm.xml")
	public void testSqlAndOrm()
	{
		// If sql or orm file are not processed at the latest this would throw an exception
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(ResourceEntity.class);
		assertNotNull(metaData);
	}
}
