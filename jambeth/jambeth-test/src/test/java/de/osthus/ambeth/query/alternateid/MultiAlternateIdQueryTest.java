package de.osthus.ambeth.query.alternateid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import de.osthus.ambeth.filter.IFilterToQueryBuilder;
import de.osthus.ambeth.filter.IPagingQuery;
import de.osthus.ambeth.filter.model.FilterDescriptor;
import de.osthus.ambeth.filter.model.IPagingRequest;
import de.osthus.ambeth.filter.model.IPagingResponse;
import de.osthus.ambeth.filter.model.ISortDescriptor;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.service.config.ConfigurationConstants;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.util.ParamChecker;

@TestProperties(name = ConfigurationConstants.mappingFile, value = "de/osthus/ambeth/query/alternateid/MultiAlternateIdQuery_orm.xml")
@SQLStructure("MultiAlternateIdQuery_structure.sql")
@SQLData("MultiAlternateIdQuery_data.sql")
public class MultiAlternateIdQueryTest extends AbstractPersistenceTest
{
	protected IFilterToQueryBuilder filterToQueryBuilder;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();
		ParamChecker.assertNotNull(filterToQueryBuilder, "filterToQueryBuilder");
	}

	public void setFilterToQueryBuilder(IFilterToQueryBuilder filterToQueryBuilder)
	{
		this.filterToQueryBuilder = filterToQueryBuilder;
	}

	@Test
	public void testRetrieveRefsFD() throws Exception
	{
		IPagingRequest pagingRequest = null;
		FilterDescriptor<MultiAlternateIdEntity> filterDescriptor = new FilterDescriptor<MultiAlternateIdEntity>(MultiAlternateIdEntity.class);
		ISortDescriptor[] sortDescriptors = null;
		IPagingQuery<MultiAlternateIdEntity> query = filterToQueryBuilder.buildQuery(filterDescriptor, sortDescriptors);
		IPagingResponse<MultiAlternateIdEntity> actual = query.retrieveRefs(pagingRequest, "AlternateId1");
		assertNotNull(actual);
		List<IObjRef> actualList = actual.getRefResult();
		assertEquals(3, actualList.size());
		assertEquals(0, actualList.get(0).getIdNameIndex());
		String aid = (String) actualList.get(0).getId();
		assertTrue(aid.endsWith(".1"));
	}

	@Test
	public void testRetrieveRefsQBF() throws Exception
	{
		IPagingRequest pagingRequest = null;
		IPagingQuery<MultiAlternateIdEntity> query = queryBuilderFactory.create(MultiAlternateIdEntity.class).buildPaging();
		IPagingResponse<MultiAlternateIdEntity> actual = query.retrieveRefs(pagingRequest, "AlternateId1");
		assertNotNull(actual);
		List<IObjRef> actualList = actual.getRefResult();
		assertEquals(3, actualList.size());
		assertEquals(0, actualList.get(0).getIdNameIndex());
		String aid = (String) actualList.get(0).getId();
		assertTrue(aid.endsWith(".1"));

		IPagingResponse<MultiAlternateIdEntity> actual2 = query.param("abc", "abcValue").retrieveRefs(pagingRequest, "AlternateId1");
		assertNotNull(actual2);
		List<IObjRef> actualList2 = actual2.getRefResult();
		assertEquals(3, actualList2.size());
		assertEquals(0, actualList2.get(0).getIdNameIndex());
		String aid2 = (String) actualList2.get(0).getId();
		assertTrue(aid2.endsWith(".1"));
	}

}
