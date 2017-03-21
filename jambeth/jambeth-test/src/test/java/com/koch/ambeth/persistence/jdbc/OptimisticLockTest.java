package com.koch.ambeth.persistence.jdbc;

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

import javax.persistence.OptimisticLockException;

import org.junit.Test;

import com.koch.ambeth.mapping.IMapperService;
import com.koch.ambeth.mapping.IMapperServiceFactory;
import com.koch.ambeth.model.Material;
import com.koch.ambeth.persistence.config.PersistenceConfigurationConstants;
import com.koch.ambeth.service.IMaterialService;
import com.koch.ambeth.service.TestServicesModule;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLData;
import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.transfer.MaterialVO;

@TestModule({ TestServicesModule.class })
@TestPropertiesList({ @TestProperties(name = PersistenceConfigurationConstants.DatabaseTablePrefix, value = "D_"),
		@TestProperties(name = PersistenceConfigurationConstants.DatabaseFieldPrefix, value = "F_"),
		@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "orm.xml"),
		@TestProperties(name = ServiceConfigurationConstants.valueObjectFile, value = "com/koch/ambeth/persistence/jdbc/mapping/value-object.xml"),
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
		material.setVersion((short) -1); // Force OptLock exception
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
