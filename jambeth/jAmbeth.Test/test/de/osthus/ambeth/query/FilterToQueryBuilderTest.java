package de.osthus.ambeth.query;

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.filter.IFilterToQueryBuilder;
import de.osthus.ambeth.filter.IPagingQuery;
import de.osthus.ambeth.filter.model.CompositeFilterDescriptor;
import de.osthus.ambeth.filter.model.FilterDescriptor;
import de.osthus.ambeth.filter.model.FilterOperator;
import de.osthus.ambeth.filter.model.IFilterDescriptor;
import de.osthus.ambeth.filter.model.IPagingResponse;
import de.osthus.ambeth.filter.model.LogicalOperator;
import de.osthus.ambeth.service.config.ConfigurationConstants;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.util.ParamChecker;

@SQLData("FilterDescriptor_data.sql")
@SQLStructure("FilterDescriptor_structure.sql")
@TestProperties(name = ConfigurationConstants.mappingFile, value = "de/osthus/ambeth/query/FilterDescriptor_orm.xml")
public class FilterToQueryBuilderTest extends AbstractPersistenceTest
{
	private IFilterToQueryBuilder filterToQueryBuilder;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(filterToQueryBuilder, "FilterToQueryBuilder");
	}

	public void setFilterToQueryBuilder(IFilterToQueryBuilder filterToQueryBuilder)
	{
		this.filterToQueryBuilder = filterToQueryBuilder;
	}

	@Test
	public void testBuildQuery_ManyChildFilters()
	{
		CompositeFilterDescriptor<QueryEntity> filterDescriptor = new CompositeFilterDescriptor<QueryEntity>(QueryEntity.class);
		filterDescriptor.setLogicalOperator(LogicalOperator.OR);
		List<IFilterDescriptor<QueryEntity>> childFilterDescriptors = new ArrayList<IFilterDescriptor<QueryEntity>>();

		for (int i = 0; i < 5000; i++)
		{
			FilterDescriptor<QueryEntity> filter1 = new FilterDescriptor<QueryEntity>(QueryEntity.class);
			filter1.setMember("Id");
			filter1.setOperator(FilterOperator.IS_EQUAL_TO);
			filter1.setValue(Arrays.asList(Integer.toString(i)));

			FilterDescriptor<QueryEntity> filter2 = new FilterDescriptor<QueryEntity>(QueryEntity.class);
			filter2.setMember("Version");
			filter2.setOperator(FilterOperator.IS_EQUAL_TO);
			filter2.setValue(Arrays.asList(Integer.toString(2)));

			CompositeFilterDescriptor<QueryEntity> childFilter = new CompositeFilterDescriptor<QueryEntity>(QueryEntity.class);
			childFilter.setLogicalOperator(LogicalOperator.AND);
			@SuppressWarnings("unchecked")
			List<IFilterDescriptor<QueryEntity>> subFilters = Arrays.<IFilterDescriptor<QueryEntity>> asList(filter1, filter2);
			childFilter.setChildFilterDescriptors(subFilters);
			childFilterDescriptors.add(childFilter);
		}

		filterDescriptor.setChildFilterDescriptors(childFilterDescriptors);
		IPagingQuery<QueryEntity> query = filterToQueryBuilder.buildQuery(filterDescriptor, null);
		assertNotNull(query);

		IPagingResponse<QueryEntity> result = query.retrieve(null);
		assertNotNull(result);
	}
}
