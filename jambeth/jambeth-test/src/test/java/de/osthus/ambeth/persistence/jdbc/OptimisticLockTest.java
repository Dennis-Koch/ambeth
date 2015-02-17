package de.osthus.ambeth.persistence.jdbc;

import javax.persistence.OptimisticLockException;

import org.junit.Test;

import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.mapping.IMapperService;
import de.osthus.ambeth.mapping.IMapperServiceFactory;
import de.osthus.ambeth.model.Material;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.service.IMaterialService;
import de.osthus.ambeth.service.TestServicesModule;
import de.osthus.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;
import de.osthus.ambeth.transfer.MaterialVO;

@TestModule({ TestServicesModule.class })
@TestPropertiesList({ @TestProperties(name = PersistenceConfigurationConstants.DatabaseTablePrefix, value = "D_"),
		@TestProperties(name = PersistenceConfigurationConstants.DatabaseFieldPrefix, value = "F_"),
		@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "orm.xml"),
		@TestProperties(name = ServiceConfigurationConstants.valueObjectFile, value = "de/osthus/ambeth/persistence/jdbc/mapping/value-object.xml"),
		@TestProperties(name = ServiceConfigurationConstants.GenericTransferMapping, value = "true") })
@SQLStructure("JDBCDatabase_structure.sql")
@SQLData("Example_data.sql")
public class OptimisticLockTest extends AbstractInformationBusWithPersistenceTest
{
	@Test(expected = OptimisticLockException.class)
	public void doesOptimisticLockException()
	{
		IMaterialService materialService = beanContext.getService(IMaterialService.class);
		Material material = materialService.getMaterialByName("test 1");
		material.setName(material.getName() + "_X");
		material.setVersion((short) (material.getVersion() - 1)); // Force OptLock exception
		materialService.updateMaterial(material);
	}

	@Test
	public void doesNoOptimisticLockExceptionWithoutChange()
	{
		IMaterialService materialService = beanContext.getService(IMaterialService.class);
		Material material = materialService.getMaterialByName("test 1");
		material.setVersion((short) (material.getVersion() - 1)); // Force OptLock exception

		// Will work without exception because nothing has been changed
		materialService.updateMaterial(material);
	}

	@Test(expected = OptimisticLockException.class)
	public void mapToVOToBO() throws Exception
	{
		IMapperServiceFactory mapperServiceFactory = beanContext.getService(IMapperServiceFactory.class);

		IMapperService mapperService = mapperServiceFactory.create();
		try
		{
			MaterialVO materialVO = new MaterialVO();
			materialVO.setName("test 3");
			materialVO.setId(3);
			materialVO.setVersion((short) 1);

			Material material = mapperService.mapToBusinessObject(materialVO);
			IMaterialService materialService = beanContext.getService(IMaterialService.class);

			materialService.updateMaterial(material);
		}
		finally
		{
			mapperService.dispose();
		}
	}

	@Test
	public void doesNotOptimisticLockException1()
	{
		IMaterialService materialService = beanContext.getService(IMaterialService.class);
		Material material = materialService.getMaterialByName("test 1");
		material.setName(material.getName() + "_X");
		materialService.updateMaterial(material);
	}

	@Test
	public void doesNotOptimisticLockException2()
	{
		IMaterialService materialService = beanContext.getService(IMaterialService.class);
		Material material = materialService.getMaterialByName("test 1");
		material.setName(material.getName() + "_X");
		material.setVersion((short) (material.getVersion() + 5));
		materialService.updateMaterial(material);
	}
}
