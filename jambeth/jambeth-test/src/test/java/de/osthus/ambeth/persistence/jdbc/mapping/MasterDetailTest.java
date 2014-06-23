package de.osthus.ambeth.persistence.jdbc.mapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.mapping.IMapperService;
import de.osthus.ambeth.mapping.IMapperServiceFactory;
import de.osthus.ambeth.merge.IMergeProcess;
import de.osthus.ambeth.persistence.jdbc.mapping.models.Detail;
import de.osthus.ambeth.persistence.jdbc.mapping.models.DetailListVO;
import de.osthus.ambeth.persistence.jdbc.mapping.models.DetailVO;
import de.osthus.ambeth.persistence.jdbc.mapping.models.Master;
import de.osthus.ambeth.persistence.jdbc.mapping.models.MasterVO;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;
import de.osthus.ambeth.util.ParamChecker;

@SQLStructure("Master_Detail_structure.sql")
@TestPropertiesList({ @TestProperties(name = ServiceConfigurationConstants.GenericTransferMapping, value = "true"),
		@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = MasterDetailTest.basePath + "master-detail-orm.xml"),
		@TestProperties(name = ServiceConfigurationConstants.valueObjectFile, value = MasterDetailTest.basePath + "master-detail-value-object.xml") })
public class MasterDetailTest extends AbstractPersistenceTest
{
	public static final String basePath = "de/osthus/ambeth/persistence/jdbc/mapping/";

	private IMapperServiceFactory mapperServiceFactory;

	private IMapperService mapperService;

	private IMergeProcess mergeProcess;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(mapperServiceFactory, "mapperServiceFactory");
		ParamChecker.assertNotNull(mergeProcess, "mergeProcess");
	}

	public void setMapperServiceFactory(IMapperServiceFactory mapperServiceFactory)
	{
		this.mapperServiceFactory = mapperServiceFactory;
	}

	public void setProcess(IMergeProcess mergeProcess)
	{
		this.mergeProcess = mergeProcess;
	}

	@Before
	public void setUp() throws Exception
	{
		mapperService = mapperServiceFactory.create();
	}

	@Test
	public void testNewVOs()
	{
		MasterVO masterVO = new MasterVO();
		masterVO.setDetails(new DetailListVO());
		masterVO.getDetails().getDetails()
				.addAll(Arrays.asList(new DetailVO(), new DetailVO(), new DetailVO(), new DetailVO(), new DetailVO(), new DetailVO()));

		Master masterBO = mapperService.mapToBusinessObject(masterVO);
		assertNotNull(masterBO);
		assertEquals(masterVO.getDetails().getDetails().size(), masterBO.getDetails().size());

		mergeProcess.process(masterBO, null, null, null);
	}

	@Test
	public void testNewBOs()
	{
		Master masterBO = entityFactory.createEntity(Master.class);
		masterBO.setDetails(Arrays.asList(entityFactory.createEntity(Detail.class), entityFactory.createEntity(Detail.class),
				entityFactory.createEntity(Detail.class), entityFactory.createEntity(Detail.class), entityFactory.createEntity(Detail.class),
				entityFactory.createEntity(Detail.class)));

		MasterVO masterVO = mapperService.mapToValueObject(masterBO, MasterVO.class);
		assertNotNull(masterVO);
		assertEquals(masterBO.getDetails().size(), masterVO.getDetails().getDetails().size());
	}

	@Test()
	public void testExistingVOs()
	{
		List<Detail> detailBOs = Arrays.asList(entityFactory.createEntity(Detail.class), entityFactory.createEntity(Detail.class),
				entityFactory.createEntity(Detail.class), entityFactory.createEntity(Detail.class), entityFactory.createEntity(Detail.class),
				entityFactory.createEntity(Detail.class));
		Master masterBO_orig = entityFactory.createEntity(Master.class);
		masterBO_orig.setDetails(detailBOs);

		mergeProcess.process(masterBO_orig, null, null, null);

		MasterVO masterVO1 = new MasterVO();
		masterVO1.setId(masterBO_orig.getId());
		masterVO1.setVersion(masterBO_orig.getVersion());
		masterVO1.setDetails(new DetailListVO());

		MasterVO masterVO2 = new MasterVO();
		masterVO2.setDetails(new DetailListVO());

		List<MasterVO> masterVOs = Arrays.asList(masterVO1, masterVO2);

		for (int i = 0, size = detailBOs.size(); i < size; i++)
		{
			Detail detailBO = detailBOs.get(i);
			DetailVO detailVO = new DetailVO();

			detailVO.setId(detailBO.getId());
			detailVO.setVersion(detailBO.getVersion());

			masterVOs.get(i % 2).getDetails().getDetails().add(detailVO);
		}

		List<Master> masterBOs = mapperService.mapToBusinessObjectList(masterVOs);
		assertNotNull(masterBOs);
		assertEquals(2, masterBOs.size());
		assertEquals(masterVO1.getDetails().getDetails().size(), masterBOs.get(0).getDetails().size());
		assertEquals(masterVO2.getDetails().getDetails().size(), masterBOs.get(1).getDetails().size());

		mergeProcess.process(masterBOs, null, null, null);
	}
}
