package com.koch.ambeth.persistence.jdbc.array;

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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.persistence.jdbc.array.ArrayTest.ArrayTestModule;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLData;
import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IList;

@SQLData("array_data.sql")
@SQLStructure("array_structure.sql")
@TestModule(ArrayTestModule.class)
@TestProperties(name = ServiceConfigurationConstants.mappingFile,
		value = "com/koch/ambeth/persistence/jdbc/array/array_orm.xml")
public class ArrayTest extends AbstractInformationBusWithPersistenceTest {
	public static class ArrayTestModule implements IInitializingModule {
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
			beanContextFactory.registerAutowireableBean(IArrayObjectService.class,
					ArrayObjectService.class);
		}
	}

	protected IArrayObjectService arrayObjectService;

	protected ArrayObject arrayObject;

	@Override
	public void afterPropertiesSet() throws Throwable {
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(arrayObjectService, "arrayObjectService");
	}

	public void setArrayObjectService(IArrayObjectService arrayObjectService) {
		this.arrayObjectService = arrayObjectService;
	}

	@Before
	public void setUp() throws Exception {
		arrayObject = entityFactory.createEntity(ArrayObject.class);
	}

	@Test
	public void createNullArray() {
		arrayObjectService.updateArrayObject(arrayObject);

		Assert.assertFalse("Wrong id", arrayObject.getId() == 0);
		Assert.assertEquals("Wrong version!", (short) 1, arrayObject.getVersion());
	}

	@Test
	public void createLongArray() {
		long[] array = new long[] {10, 8, Long.MIN_VALUE, Long.MAX_VALUE, 0};
		arrayObject.setArrayContentLong(array);
		arrayObjectService.updateArrayObject(arrayObject);

		arrayObject = arrayObjectService.getArrayObject(arrayObject.getId());

		Assert.assertTrue(Arrays.equals(array, arrayObject.getArrayContentLong()));
	}

	@Test
	public void createLongArray2() {
		Long[] array =
				new Long[] {new Long(10), new Long(8), Long.MIN_VALUE, Long.MAX_VALUE, new Long(0)};
		arrayObject.setArrayContentLong2(array);
		arrayObjectService.updateArrayObject(arrayObject);

		arrayObject = arrayObjectService.getArrayObject(arrayObject.getId());

		Assert.assertTrue(Arrays.equals(array, arrayObject.getArrayContentLong2()));
	}

	@Test
	public void createLongArraySet() {
		Long[] array =
				new Long[] {new Long(10), new Long(8), Long.MIN_VALUE, Long.MAX_VALUE, new Long(0)};
		Set<Long> set = new HashSet<>(Arrays.asList(array));
		arrayObject.setSetContentLong(set);
		arrayObjectService.updateArrayObject(arrayObject);

		arrayObject = arrayObjectService.getArrayObject(arrayObject.getId());

		Set<Long> actual = arrayObject.getSetContentLong();
		Assert.assertEquals(set.size(), actual.size());
		Assert.assertTrue(set.containsAll(actual));
	}

	@Test
	public void createLongArrayList() {
		Long[] array =
				new Long[] {new Long(10), new Long(8), Long.MIN_VALUE, Long.MAX_VALUE, new Long(0)};
		ArrayList<Long> list = new ArrayList<>(array);
		arrayObject.setListContentLong(list);
		arrayObjectService.updateArrayObject(arrayObject);

		arrayObject = arrayObjectService.getArrayObject(arrayObject.getId());

		List<Long> actual = arrayObject.getListContentLong();
		Assert.assertEquals(list.size(), actual.size());
		Assert.assertTrue(list.containsAll(actual));
	}

	@Test
	public void createLongArrayColl() {
		Long[] array =
				new Long[] {new Long(10), new Long(8), Long.MIN_VALUE, Long.MAX_VALUE, new Long(0)};
		ArrayList<Long> list = new ArrayList<>(array);
		arrayObject.setCollContentLong(list);
		arrayObjectService.updateArrayObject(arrayObject);

		arrayObject = arrayObjectService.getArrayObject(arrayObject.getId());

		Assert.assertEquals(list.size(), arrayObject.getCollContentLong().size());
		Assert.assertTrue(list.containsAll(arrayObject.getCollContentLong()));
	}

	@Test
	public void createIntArray() {
		int[] array = new int[] {10, 8, Integer.MIN_VALUE, Integer.MAX_VALUE, 0};
		arrayObject.setArrayContentInt(array);
		arrayObjectService.updateArrayObject(arrayObject);

		arrayObject = arrayObjectService.getArrayObject(arrayObject.getId());

		Assert.assertTrue(Arrays.equals(array, arrayObject.getArrayContentInt()));
	}

	@Test
	public void createIntArray2() {
		Integer[] array = new Integer[] {10, 8, Integer.MIN_VALUE, Integer.MAX_VALUE, 0};
		arrayObject.setArrayContentInt2(array);
		arrayObjectService.updateArrayObject(arrayObject);

		arrayObject = arrayObjectService.getArrayObject(arrayObject.getId());

		Assert.assertTrue(Arrays.equals(array, arrayObject.getArrayContentInt2()));
	}

	@Test
	public void createShortArray() {
		short[] array = new short[] {10, 8, Short.MIN_VALUE, Short.MAX_VALUE, 0};
		arrayObject.setArrayContentShort(array);
		arrayObjectService.updateArrayObject(arrayObject);

		arrayObject = arrayObjectService.getArrayObject(arrayObject.getId());

		Assert.assertTrue(Arrays.equals(array, arrayObject.getArrayContentShort()));
	}

	@Test
	public void createShortArray2() {
		Short[] array = new Short[] {Short.valueOf((short) 10), Short.valueOf((short) 8),
				Short.MIN_VALUE, Short.MAX_VALUE, Short.valueOf((short) 0)};
		arrayObject.setArrayContentShort2(array);
		arrayObjectService.updateArrayObject(arrayObject);

		arrayObject = arrayObjectService.getArrayObject(arrayObject.getId());

		Assert.assertTrue(Arrays.equals(array, arrayObject.getArrayContentShort2()));
	}

	@Test
	public void createByteArray() {
		byte[] array = new byte[] {(byte) 0, Byte.MIN_VALUE, Byte.MAX_VALUE, (byte) 100};
		arrayObject.setArrayContentByte(array);
		arrayObjectService.updateArrayObject(arrayObject);

		arrayObject = arrayObjectService.getArrayObject(arrayObject.getId());

		Assert.assertTrue(Arrays.equals(array, arrayObject.getArrayContentByte()));
	}

	@Test
	public void createByteArray2() {
		Byte[] array = new Byte[] {(byte) 0, Byte.MIN_VALUE, Byte.MAX_VALUE, (byte) 100};
		arrayObject.setArrayContentByte2(array);
		arrayObjectService.updateArrayObject(arrayObject);

		arrayObject = arrayObjectService.getArrayObject(arrayObject.getId());

		Assert.assertTrue(Arrays.equals(array, arrayObject.getArrayContentByte2()));
	}

	@Test
	public void createCharArray() {
		char[] array = new char[] {10, 0, Character.MIN_VALUE, Character.MAX_VALUE, 13};
		arrayObject.setArrayContentChar(array);
		arrayObjectService.updateArrayObject(arrayObject);

		arrayObject = arrayObjectService.getArrayObject(arrayObject.getId());

		Assert.assertTrue(Arrays.equals(array, arrayObject.getArrayContentChar()));
	}

	// TODO Character.MAX_VALUE is to big for DB type CHAR (NCHAR works)
	@Test
	public void createCharArray2() {
		Character[] array = new Character[] {10, 0, Character.MIN_VALUE, Character.MAX_VALUE, 13};
		arrayObject.setArrayContentChar2(array);
		arrayObjectService.updateArrayObject(arrayObject);

		arrayObject = arrayObjectService.getArrayObject(arrayObject.getId());

		Assert.assertTrue(Arrays.equals(array, arrayObject.getArrayContentChar2()));
	}

	@Test
	public void createBooleanArray() {
		boolean[] array = new boolean[] {true, false, true, true};
		arrayObject.setArrayContentBool(array);
		arrayObjectService.updateArrayObject(arrayObject);

		arrayObject = arrayObjectService.getArrayObject(arrayObject.getId());

		Assert.assertTrue(Arrays.equals(array, arrayObject.getArrayContentBool()));
	}

	@Test
	public void createBooleanArray2() {
		Boolean[] array = new Boolean[] {true, false, true, true};
		arrayObject.setArrayContentBool2(array);
		arrayObjectService.updateArrayObject(arrayObject);

		arrayObject = arrayObjectService.getArrayObject(arrayObject.getId());

		Assert.assertTrue(Arrays.equals(array, arrayObject.getArrayContentBool2()));
	}

	@Test
	public void createDoubleArray() {
		double[] array = new double[] {0, 17, 1234567890.0123456789, 0.0000000001};
		arrayObject.setArrayContentDouble(array);
		arrayObjectService.updateArrayObject(arrayObject);

		arrayObject = arrayObjectService.getArrayObject(arrayObject.getId());

		double[] actuals = arrayObject.getArrayContentDouble();
		for (int i = 0; i < array.length; i++) {
			double expected = array[i];
			double actual = actuals[i];
			double delta = expected * 10e-15;
			Assert.assertEquals(expected, actual, delta);
		}
	}

	@Test
	public void createDoubleArray2() {
		Double[] array =
				new Double[] {new Double(0), new Double(17), 1234567890.0123456789, 0.0000000001};
		arrayObject.setArrayContentDouble2(array);
		arrayObjectService.updateArrayObject(arrayObject);

		arrayObject = arrayObjectService.getArrayObject(arrayObject.getId());

		Double[] actuals = arrayObject.getArrayContentDouble2();
		for (int i = 0; i < array.length; i++) {
			Double expected = array[i];
			Double actual = actuals[i];
			Double delta = expected * 10e-15;
			Assert.assertEquals(expected, actual, delta);
		}
	}

	@Test
	public void createFloatArray() {
		float[] array = new float[] {0, 17, Float.MAX_VALUE, Float.MIN_VALUE};
		arrayObject.setArrayContentFloat(array);
		arrayObjectService.updateArrayObject(arrayObject);

		arrayObject = arrayObjectService.getArrayObject(arrayObject.getId());

		Assert.assertTrue(Arrays.equals(array, arrayObject.getArrayContentFloat()));
	}

	@Test
	public void createFloatArray2() {
		Float[] array = new Float[] {new Float(0), new Float(17), Float.MAX_VALUE, Float.MIN_VALUE};
		arrayObject.setArrayContentFloat2(array);
		arrayObjectService.updateArrayObject(arrayObject);

		arrayObject = arrayObjectService.getArrayObject(arrayObject.getId());

		Assert.assertTrue(Arrays.equals(array, arrayObject.getArrayContentFloat2()));
	}

	@Test
	public void createStringArray() {
		String[] array = new String[] {"Hallo", "was\ngeht.", "\t\n\rhello?"};
		arrayObject.setArrayContentString(array);
		arrayObjectService.updateArrayObject(arrayObject);

		arrayObject = arrayObjectService.getArrayObject(arrayObject.getId());

		Assert.assertTrue(Arrays.equals(array, arrayObject.getArrayContentString()));
	}

	@Test
	public void selectStringArray() {
		createStringArray();
		arrayObject = entityFactory.createEntity(ArrayObject.class);
		createStringArray();

		IQueryBuilder<ArrayObject> queryBuilder = queryBuilderFactory.create(ArrayObject.class);
		IQuery<ArrayObject> query =
				queryBuilder.build(queryBuilder.isIn(queryBuilder.property("ArrayContentString"),
						queryBuilder.value(Arrays.<String>asList("bla", "hallo")), false));

		IList<ArrayObject> result = query.retrieve();
		Assert.assertEquals(2, result.size());
	}

	@Test
	public void updateStringArray() {
		String[] array = new String[] {"Hallo", "was\ngeht.", "\t\n\rhello?"};
		arrayObject.setArrayContentString(array);
		arrayObjectService.updateArrayObject(arrayObject);
		arrayObject = arrayObjectService.getArrayObject(arrayObject.getId());

		array = new String[] {"Hallo2", "was\ngeht.2", "2\t\n\rhello?"};
		arrayObject.setArrayContentString(array);
		arrayObjectService.updateArrayObject(arrayObject);
		arrayObject = arrayObjectService.getArrayObject(arrayObject.getId());

		Assert.assertTrue(Arrays.equals(array, arrayObject.getArrayContentString()));
	}

	@Test
	public void createStringArrayList() {
		String[] array = new String[] {"Hallo", "was\ngeht.", "\t\n\rhello?"};
		ArrayList<String> list = new ArrayList<>(array);
		arrayObject.setListContentString(list);
		arrayObjectService.updateArrayObject(arrayObject);

		arrayObject = arrayObjectService.getArrayObject(arrayObject.getId());

		Assert.assertEquals(list.size(), arrayObject.getListContentString().size());
		Assert.assertTrue(list.containsAll(arrayObject.getListContentString()));
	}
}
