package de.osthus.ambeth.filter;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.filter.model.FilterDescriptor;
import de.osthus.ambeth.filter.model.FilterOperator;
import de.osthus.ambeth.filter.model.IFilterDescriptor;
import de.osthus.ambeth.filter.model.LogicalOperator;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.query.IQueryBuilderFactory;
import de.osthus.ambeth.testutil.AbstractIocTest;
import de.osthus.ambeth.testutil.TestModule;

@TestModule(FilterToQueryBuilderTestModule.class)
public class FilterToQueryBuilderTest extends AbstractIocTest
{
	@Autowired
	protected FilterToQueryBuilder filterToQueryBuilder;

	@Autowired
	protected IQueryBuilderFactory queryBuilderFactory;

	/**
	 * Check that the balanced tree contains all elements of a FilterDescriptor with 3 Elements
	 * 
	 */
	@Test
	public void test()
	{
		ArrayList<IFilterDescriptor<Object>> childFilterDescriptors = new ArrayList<IFilterDescriptor<Object>>();

		// At least 3 filters needed for test, filter 1
		FilterDescriptor<Object> filter = new FilterDescriptor<Object>();
		filter.setCaseSensitive(false);
		filter.setEntityType(Object.class);
		filter.setOperator(FilterOperator.IS_EQUAL_TO);
		filter.setMember("Member");
		filter.setValue(Arrays.asList("value1"));
		childFilterDescriptors.add(filter);

		// At least 3 filters needed for test, filter 2
		filter = new FilterDescriptor<Object>();
		filter.setCaseSensitive(false);
		filter.setEntityType(Object.class);
		filter.setOperator(FilterOperator.IS_EQUAL_TO);
		filter.setMember("Member");
		filter.setValue(Arrays.asList("value2"));
		childFilterDescriptors.add(filter);

		// At least 3 filters needed for test, filter 3
		filter = new FilterDescriptor<Object>();
		filter.setCaseSensitive(false);
		filter.setEntityType(Object.class);
		filter.setOperator(FilterOperator.IS_EQUAL_TO);
		filter.setMember("Member");
		filter.setValue(Arrays.asList("value3"));
		childFilterDescriptors.add(filter);

		// Build the Query
		IQueryBuilder<IQueryBuilderFactory> dummyQueryBuilder = queryBuilderFactory.create(IQueryBuilderFactory.class);
		IOperand balancedTree = filterToQueryBuilder.buildBalancedTree(childFilterDescriptors, LogicalOperator.OR, dummyQueryBuilder);
		Assert.assertNotNull("balancedTree is null", balancedTree);
		Assert.assertTrue("BalancedTree is not an instance of DummyOperand", balancedTree instanceof OperandDummy);

		OperandDummy dummyTree = (OperandDummy) balancedTree;
		Assert.assertEquals("or", dummyTree.getType());
		Assert.assertNotNull(dummyTree.getOperands());
		Assert.assertEquals(2, dummyTree.getOperands().length);

		// first layer left should be the "IsEqualTo"
		IOperand operand1 = dummyTree.getOperands()[0];
		Assert.assertNotNull("left part is null", operand1);
		Assert.assertTrue("left part is not an instance of DummyOperand", operand1 instanceof OperandDummy);
		OperandDummy dummyOperand1 = (OperandDummy) operand1;
		Assert.assertEquals("Left part is not of type isEqualsTo", "isEqualTo", dummyOperand1.getType());

		// first layer right should contain two operands compared by "or"
		IOperand operand2 = dummyTree.getOperands()[1];
		Assert.assertNotNull("right part is null", operand2);
		Assert.assertTrue("right part is not an instance of DummyOperand", operand2 instanceof OperandDummy);
		OperandDummy dummyOperand2 = (OperandDummy) operand2;
		Assert.assertEquals("or", dummyOperand2.getType());
		Assert.assertNotNull(dummyOperand2.getOperands());
		Assert.assertEquals(2, dummyOperand2.getOperands().length);

		// second layer right should be the "IsEqualTo" in left and right part
		IOperand operand21 = dummyOperand2.getOperands()[0];
		Assert.assertNotNull("left part is null", operand21);
		Assert.assertTrue("left part is not an instance of DummyOperand", operand21 instanceof OperandDummy);
		OperandDummy dummyOperand21 = (OperandDummy) operand21;
		Assert.assertEquals("Left part is not of type isEqualsTo", "isEqualTo", dummyOperand21.getType());

		IOperand operand22 = dummyOperand2.getOperands()[1];
		Assert.assertNotNull("left part is null", operand22);
		Assert.assertTrue("left part is not an instance of DummyOperand", operand22 instanceof OperandDummy);
		OperandDummy dummyOperand22 = (OperandDummy) operand22;
		Assert.assertEquals("Left part is not of type isEqualsTo", "isEqualTo", dummyOperand22.getType());

	}

}
