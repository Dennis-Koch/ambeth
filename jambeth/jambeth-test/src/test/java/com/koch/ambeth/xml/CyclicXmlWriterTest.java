package com.koch.ambeth.xml;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.transfer.CUDResult;
import com.koch.ambeth.merge.transfer.EntityMetaDataTransfer;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.testutil.AbstractInformationBusTest;
import com.koch.ambeth.testutil.SQLData;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.xml.ioc.BootstrapScannerModule;
import com.koch.ambeth.xml.ioc.XmlModule;
import com.koch.ambeth.xml.transfer.TestEnum;
import com.koch.ambeth.xml.transfer.TestXmlObject;

@TestModule({BootstrapScannerModule.class, XmlModule.class})
public class CyclicXmlWriterTest extends AbstractInformationBusTest {
	@Autowired(XmlModule.CYCLIC_XML_HANDLER)
	protected ICyclicXMLHandler cyclicXmlHandler;

	@Test
	public void writeSimpleCudResult() {
		CUDResult cudResult = new CUDResult();

		String xml = cyclicXmlHandler.write(cudResult);

		Assert.assertEquals("Wrong xml", XmlTestConstants.XmlOutput[0], xml);
	}

	@Test
	public void writeEntityMetaData() {
		EntityMetaDataTransfer entityMetaData = new EntityMetaDataTransfer();

		String xml = cyclicXmlHandler.write(entityMetaData);

		Assert.assertEquals("Wrong xml", XmlTestConstants.XmlOutput[1], xml);
	}

	@Test
	public void readWriteSimpleCudResult() {
		CUDResult cudResult = new CUDResult();

		String xml = cyclicXmlHandler.write(cudResult);

		Object object = cyclicXmlHandler.read(xml);

		Assert.assertSame("Wrong class", object.getClass(), CUDResult.class);
		Assert.assertEquals("Wrong xml", XmlTestConstants.XmlOutput[2], xml);
	}

	@Test
	public void writeClass() {
		Object[] array = new Object[3];

		array[0] = Class.class;
		array[1] = List.class;
		array[2] = TestXmlObject.class;

		String xml = cyclicXmlHandler.write(array);
		Object obj = cyclicXmlHandler.read(xml);

		Assert.assertSame("Wrong class", Object[].class, obj.getClass());
		Object[] result = (Object[]) obj;
		Assert.assertEquals("Wrong size", array.length, result.length);
		Assert.assertSame(array[0], result[0]);
		Assert.assertSame(array[1], result[1]);
		Assert.assertSame(array[2], result[2]);
		Assert.assertEquals("Wrong xml", XmlTestConstants.XmlOutput[3], xml);
	}

	@Test
	public void readWriteEnum() {
		Object[] array = new Object[3];

		array[0] = TestEnum.VALUE_1;
		array[1] = TestEnum.VALUE_2;
		array[2] = TestEnum.VALUE_1;

		String xml = cyclicXmlHandler.write(array);
		Object obj = cyclicXmlHandler.read(xml);

		Assert.assertSame("Wrong class", Object[].class, obj.getClass());
		Object[] result = (Object[]) obj;
		Assert.assertEquals("Wrong size", array.length, result.length);
		Assert.assertSame(array[0], result[0]);
		Assert.assertSame(array[1], result[1]);
		Assert.assertSame(array[2], result[2]);
		Assert.assertEquals("Wrong xml", XmlTestConstants.XmlOutput[4], xml);
	}

	@Test
	@Ignore
	public void readWriteSet() {
		Set<?>[] array = new Set[3];

		array[0] = new HashSet<Object>();
		array[1] = new HashSet<Object>();
		array[2] = array[1];

		String xml = cyclicXmlHandler.write(array);
		Object obj = cyclicXmlHandler.read(xml);

		Assert.assertSame("Wrong class", Set[].class, obj.getClass());
		Set<?>[] result = (Set[]) obj;
		Assert.assertEquals("Wrong size", array.length, result.length);
		Assert.assertSame(result[1], result[2]);
		Assert.assertEquals("Wrong set size at 0", array[0].size(), result[0].size());
		Assert.assertEquals("Wrong set size at 1", array[1].size(), result[1].size());
		Assert.assertEquals("Wrong set size at 2", array[2].size(), result[2].size());
		Assert.assertEquals("Wrong xml", XmlTestConstants.XmlOutput[5], xml);
	}

	@Test
	public void readWriteList() {
		List<?>[] array = new List[3];

		array[0] = new ArrayList<Object>();
		array[1] = new ArrayList<Object>();
		array[2] = array[1];

		String xml = cyclicXmlHandler.write(array);
		Object obj = cyclicXmlHandler.read(xml);

		Assert.assertSame("Wrong class", List[].class, obj.getClass());
		List<?>[] result = (List[]) obj;
		Assert.assertEquals("Wrong size", array.length, result.length);
		Assert.assertSame(result[1], result[2]);
		Assert.assertEquals("Wrong set size at 0", array[0].size(), result[0].size());
		Assert.assertEquals("Wrong set size at 1", array[1].size(), result[1].size());
		Assert.assertEquals("Wrong set size at 2", array[2].size(), result[2].size());
		Assert.assertEquals("Wrong xml", XmlTestConstants.XmlOutput[6], xml);
	}

	@TestProperties(name = ServiceConfigurationConstants.mappingFile,
			value = "com/koch/ambeth/xml/xml_orm.xml")
	@SQLData("xml_data.sql")
	@Test
	public void writeObjRefs() {
		IObjRef[] allOris = new IObjRef[4];

		Short shortFour = Short.valueOf((short) 4);

		ObjRef ori = new ObjRef(Entity.class, 2, shortFour);
		allOris[0] = ori;
		String xml = cyclicXmlHandler.write(ori);
		Assert.assertEquals("Wrong xml", XmlTestConstants.XmlOutput[15], xml);
		Object actual = cyclicXmlHandler.read(xml);
		Assert.assertTrue("Wrong class", actual instanceof IObjRef);
		assertObjRefEquals(ori, (IObjRef) actual);

		ori = new ObjRef(Entity.class, ObjRef.PRIMARY_KEY_INDEX, 2, shortFour);
		allOris[1] = ori;
		xml = cyclicXmlHandler.write(ori);
		Assert.assertEquals("Wrong xml", XmlTestConstants.XmlOutput[15], xml);
		actual = cyclicXmlHandler.read(xml);
		Assert.assertTrue("Wrong class", actual instanceof IObjRef);
		assertObjRefEquals(ori, (IObjRef) actual);

		ori = new ObjRef(Entity.class, (byte) 0, "zwei", shortFour);
		allOris[2] = ori;
		xml = cyclicXmlHandler.write(ori);
		Assert.assertEquals("Wrong xml", XmlTestConstants.XmlOutput[16], xml);
		actual = cyclicXmlHandler.read(xml);
		Assert.assertTrue("Wrong class", actual instanceof IObjRef);
		assertObjRefEquals(ori, (IObjRef) actual);

		ori = new ObjRef(Entity.class, (byte) 1, "zwei", shortFour);
		allOris[3] = ori;
		xml = cyclicXmlHandler.write(ori);
		Assert.assertEquals("Wrong xml", XmlTestConstants.XmlOutput[17], xml);
		actual = cyclicXmlHandler.read(xml);
		Assert.assertTrue("Wrong class", actual instanceof IObjRef);
		assertObjRefEquals(ori, (IObjRef) actual);

		xml = cyclicXmlHandler.write(allOris);
		actual = cyclicXmlHandler.read(xml);
		Assert.assertEquals(allOris.getClass(), actual.getClass());
		IObjRef[] actualArray = (IObjRef[]) actual;
		Assert.assertEquals(allOris.length, actualArray.length);
		for (int i = 0; i < allOris.length; i++) {
			assertObjRefEquals(allOris[i], actualArray[i]);
		}
	}

	@Test
	public void nativeInteger() {
		int[] array = new int[3];

		array[0] = Integer.MAX_VALUE;
		array[1] = Integer.MIN_VALUE;
		array[2] = 3;

		String xml = cyclicXmlHandler.write(array);
		Object obj = cyclicXmlHandler.read(xml);

		Assert.assertSame("Wrong class", int[].class, obj.getClass());
		int[] result = (int[]) obj;
		Assert.assertEquals("Wrong size", array.length, result.length);
		Assert.assertEquals(array[0], result[0]);
		Assert.assertEquals(array[1], result[1]);
		Assert.assertEquals(array[2], result[2]);
		Assert.assertEquals("Wrong xml", XmlTestConstants.XmlOutput[7], xml);
	}

	@Test
	public void nativeLong() {
		long[] array = new long[3];

		array[0] = Long.MAX_VALUE;
		array[1] = Long.MIN_VALUE;
		array[2] = 3;

		String xml = cyclicXmlHandler.write(array);
		Object obj = cyclicXmlHandler.read(xml);

		Assert.assertSame("Wrong class", long[].class, obj.getClass());
		long[] result = (long[]) obj;
		Assert.assertEquals("Wrong size", array.length, result.length);
		Assert.assertEquals(array[0], result[0]);
		Assert.assertEquals(array[1], result[1]);
		Assert.assertEquals(array[2], result[2]);
		Assert.assertEquals("Wrong xml", XmlTestConstants.XmlOutput[8], xml);
	}

	@Test
	public void nativeDouble() {
		double[] array = new double[3];

		array[0] = Double.MAX_VALUE;
		array[1] = Double.MIN_VALUE;
		array[2] = 3;

		String xml = cyclicXmlHandler.write(array);
		Object obj = cyclicXmlHandler.read(xml);

		Assert.assertSame("Wrong class", double[].class, obj.getClass());
		double[] result = (double[]) obj;
		Assert.assertEquals("Wrong size", array.length, result.length);
		Assert.assertEquals(array[0], result[0], Double.MIN_VALUE);
		Assert.assertEquals(array[1], result[1], Double.MIN_VALUE);
		Assert.assertEquals(array[2], result[2], Double.MIN_VALUE);
		Assert.assertEquals("Wrong xml", XmlTestConstants.XmlOutput[9], xml);
	}

	@Test
	public void nativeFloat() {
		float[] array = new float[3];

		array[0] = Float.MAX_VALUE;
		array[1] = Float.MIN_VALUE;
		array[2] = 3;

		String xml = cyclicXmlHandler.write(array);
		Object obj = cyclicXmlHandler.read(xml);

		Assert.assertSame("Wrong class", float[].class, obj.getClass());
		float[] result = (float[]) obj;
		Assert.assertEquals("Wrong size", array.length, result.length);
		Assert.assertEquals(array[0], result[0], Double.MIN_VALUE);
		Assert.assertEquals(array[1], result[1], Double.MIN_VALUE);
		Assert.assertEquals(array[2], result[2], Double.MIN_VALUE);
		Assert.assertEquals("Wrong xml", XmlTestConstants.XmlOutput[10], xml);
	}

	@Test
	public void nativeShort() {
		short[] array = new short[3];

		array[0] = Short.MAX_VALUE;
		array[1] = Short.MIN_VALUE;
		array[2] = 3;

		String xml = cyclicXmlHandler.write(array);
		Object obj = cyclicXmlHandler.read(xml);

		Assert.assertSame("Wrong class", short[].class, obj.getClass());
		short[] result = (short[]) obj;
		Assert.assertEquals("Wrong size", array.length, result.length);
		Assert.assertEquals(array[0], result[0]);
		Assert.assertEquals(array[1], result[1]);
		Assert.assertEquals(array[2], result[2]);
		Assert.assertEquals("Wrong xml", XmlTestConstants.XmlOutput[11], xml);
	}

	@Test
	public void nativeByte() {
		byte[] array = new byte[3];

		array[0] = Byte.MAX_VALUE;
		array[1] = Byte.MIN_VALUE;
		array[2] = 3;

		String xml = cyclicXmlHandler.write(array);
		Object obj = cyclicXmlHandler.read(xml);

		Assert.assertSame("Wrong class", byte[].class, obj.getClass());
		byte[] result = (byte[]) obj;
		Assert.assertEquals("Wrong size", array.length, result.length);
		Assert.assertEquals(array[0], result[0]);
		Assert.assertEquals(array[1], result[1]);
		Assert.assertEquals(array[2], result[2]);
		Assert.assertEquals("Wrong xml", XmlTestConstants.XmlOutput[12], xml);
	}

	@Test
	public void nativeCharacter() {
		char[] array = new char[3];

		array[0] = Character.MAX_VALUE;
		array[1] = Character.MIN_VALUE;
		array[2] = 3;

		String xml = cyclicXmlHandler.write(array);
		Object obj = cyclicXmlHandler.read(xml);

		Assert.assertSame("Wrong class", char[].class, obj.getClass());
		char[] result = (char[]) obj;
		Assert.assertEquals("Wrong size", array.length, result.length);
		Assert.assertEquals(array[0], result[0]);
		Assert.assertEquals(array[1], result[1]);
		Assert.assertEquals(array[2], result[2]);
		Assert.assertEquals("Wrong xml", XmlTestConstants.XmlOutput[13], xml);
	}

	@Test
	public void nativeBoolean() {
		boolean[] array = new boolean[3];
		array[0] = Boolean.TRUE;
		array[1] = Boolean.FALSE;
		array[2] = Boolean.TRUE;

		String xml = cyclicXmlHandler.write(array);
		Object obj = cyclicXmlHandler.read(xml);

		Assert.assertSame("Wrong class", boolean[].class, obj.getClass());
		boolean[] result = (boolean[]) obj;
		Assert.assertEquals("Wrong size", array.length, result.length);
		Assert.assertEquals(array[0], result[0]);
		Assert.assertEquals(array[1], result[1]);
		Assert.assertEquals(array[2], result[2]);
		Assert.assertEquals("Wrong xml", XmlTestConstants.XmlOutput[14], xml);
	}

	@Test
	public void cyclicString() {
		String[] array = new String[4];
		array[0] = "HalloEinfach]]";
		array[1] = "Hall\n\to]]>MeinTest]]>AB]]AB]>AB";
		array[2] = array[0];
		array[3] = "hallo2";

		String[] result = checkCyclic(array, String[].class);
		Assert.assertSame(result[2], result[0]);
	}

	@Test
	public void cyclicString2() {
		String xml =
				"<root><a i=\"1\" s=\"2\" ti=\"2\" n=\"Object\"><o i=\"3\" ti=\"4\" n=\"CUDResult\" m=\"AllChanges\"><l i=\"5\" s=\"2\" ti=\"6\" n=\"com.koch.ambeth.merge.model.IChangeContainer\"><o i=\"7\" ti=\"8\" n=\"CreateContainer\" m=\"Primitives Reference Relations\"><a i=\"9\" s=\"4\" ti=\"10\" n=\"IPUI\"><o i=\"11\" ti=\"12\" n=\"PrimitiveUpdateItem\" m=\"NewValue MemberName\"><e i=\"13\" ti=\"14\" n=\"OrderStateType\" ns=\"Comtrack\" v=\"OPEN\"/><s i=\"15\"><![CDATA[State]]></s></o><o i=\"16\" ti=\"12\" m=\"NewValue MemberName\"><e i=\"17\" ti=\"18\" n=\"OrderType\" ns=\"Comtrack\" v=\"FTE\"/><s i=\"19\"><![CDATA[OrderType]]></s></o><o i=\"20\" ti=\"12\" m=\"NewValue MemberName\"><s i=\"21\"/><s i=\"22\"><![CDATA[Workgroup]]></s></o><o i=\"23\" ti=\"12\" m=\"NewValue MemberName\"><s i=\"24\"><![CDATA[test4]]></s><s i=\"25\"><![CDATA[Supplier]]></s></o></a><o i=\"26\" ti=\"27\" n=\"DirectObjRef\" m=\"RealType IdNameIndex CreateContainerIndex\"><c i=\"28\" n=\"Ordr\" ns=\"Comtrack\"/><o i=\"29\" ti=\"30\" n=\"ByteN\" v=\"-1\"/><o i=\"31\" ti=\"32\" n=\"Int32N\" v=\"1\"/></o><a i=\"33\" s=\"1\" ti=\"34\" n=\"IRUI\"><o i=\"35\" ti=\"36\" n=\"RelationUpdateItem\" m=\"AddedORIs MemberName\"><a i=\"37\" s=\"1\" ti=\"38\" n=\"com.koch.ambeth.merge.model.IObjRef\"><o i=\"39\" ti=\"27\"><c i=\"40\" n=\"Compound\" ns=\"Comtrack\"/><r i=\"29\"/><r i=\"31\"/></o></a><s i=\"41\"><![CDATA[Compounds]]></s></o></a></o><o i=\"42\" ti=\"8\" m=\"Primitives Reference Relations\"><a i=\"43\" s=\"3\" ti=\"10\"><o i=\"44\" ti=\"12\" m=\"NewValue MemberName\"><s i=\"45\"><![CDATA[test4]]></s><s i=\"46\"><![CDATA[Description]]></s></o><o i=\"47\" ti=\"12\" m=\"NewValue MemberName\"><e i=\"48\" ti=\"49\" n=\"CompoundStateType\" ns=\"Comtrack\" v=\"INITIALIZED\"/><s i=\"50\"><![CDATA[State]]></s></o><o i=\"51\" ti=\"12\" m=\"NewValue MemberName\"><o ti=\"52\" n=\"Int64\" v=\"1\"/><s i=\"53\"><![CDATA[DetailNumber]]></s></o></a><r i=\"39\"/><a i=\"54\" s=\"1\" ti=\"34\"><o i=\"55\" ti=\"36\" m=\"AddedORIs MemberName\"><a i=\"56\" s=\"1\" ti=\"38\"><r i=\"26\"/></a><s i=\"57\"><![CDATA[Order]]></s></o></a></o></l></o><o i=\"58\" ti=\"59\" n=\"MethodDescription\" m=\"ServiceType ParamTypes MethodName\"><c i=\"60\" n=\"IOrderService\"/><a i=\"61\" s=\"2\" ti=\"62\" n=\"Class\"><c i=\"63\" n=\"Ordr[]\" ns=\"Comtrack\"/><r i=\"63\"/></a><s i=\"64\"><![CDATA[Save]]></s></o></a></root>";
		cyclicXmlHandler.read(xml);
	}

	@Test
	public void cyclicBoolean() {
		Boolean[] array = new Boolean[3];
		array[0] = Boolean.TRUE;
		array[1] = Boolean.FALSE;
		array[2] = Boolean.TRUE;

		Boolean[] result = checkCyclic(array, Boolean[].class);
		Assert.assertSame(result[2], result[0]);
	}

	@Test
	public void cyclicByte() {
		Byte[] array = new Byte[3];
		array[0] = Byte.valueOf((byte) 10);
		array[1] = Byte.valueOf((byte) 20);
		array[2] = array[0];

		Byte[] result = checkCyclic(array, Byte[].class);
		Assert.assertSame(result[2], result[0]);
	}

	@Test
	public void cyclicCharacter() {
		Character[] array = new Character[3];
		array[0] = Character.valueOf('A');
		array[1] = Character.valueOf('&');
		array[2] = array[0];

		Character[] result = checkCyclic(array, Character[].class);
		Assert.assertSame(result[2], result[0]);
	}

	@Test
	public void cyclicShort() {
		Short[] array = new Short[3];
		array[0] = Short.MAX_VALUE;
		array[1] = Short.MIN_VALUE;
		array[2] = array[0];

		Short[] result = checkCyclic(array, Short[].class);
		Assert.assertSame(result[2], result[0]);
	}

	@Test
	public void cyclicFloat() {
		Float[] array = new Float[3];
		array[0] = Float.MAX_VALUE;
		array[1] = Float.MIN_VALUE;
		array[2] = array[0];

		Float[] result = checkCyclic(array, Float[].class);
		Assert.assertSame(result[2], result[0]);
	}

	@Test
	public void cyclicInteger() {
		Integer[] array = new Integer[3];
		array[0] = Integer.MAX_VALUE;
		array[1] = Integer.MIN_VALUE;
		array[2] = array[0];

		Integer[] result = checkCyclic(array, Integer[].class);
		Assert.assertSame(result[2], result[0]);
	}

	@Test
	public void cyclicDouble() {
		Double[] array = new Double[3];
		array[0] = Double.MAX_VALUE;
		array[1] = Double.MIN_VALUE;
		array[2] = array[0];

		Double[] result = checkCyclic(array, Double[].class);
		Assert.assertSame(result[2], result[0]);
	}

	@Test
	public void cyclicLong() {
		Long[] array = new Long[3];
		array[0] = Long.MAX_VALUE;
		array[1] = Long.MIN_VALUE;
		array[2] = array[0];

		Long[] result = checkCyclic(array, Long[].class);
		Assert.assertSame(result[2], result[0]);
	}

	@Test
	public void cyclicDate() {
		Date[] array = new Date[3];
		array[0] = new Date(50000000L);
		array[1] = new Date(35000000L);
		array[2] = array[0];

		Date[] result = checkCyclic(array, Date[].class);
		Assert.assertSame(result[2], result[0]);
	}

	@Test
	public void cyclicTestXmlObject() {
		TestXmlObject[] array = new TestXmlObject[3];
		array[0] = new TestXmlObject();
		array[1] = new TestXmlObject();
		array[2] = array[0];
		fillTestXmlObject(array[0], 23);
		fillTestXmlObject(array[1], 46);

		TestXmlObject[] result = checkCyclic(array, TestXmlObject[].class);
		Assert.assertSame(result[2], result[0]);
		Assert.assertNull(result[0].getTransientField1());
		Assert.assertNull(result[0].getTransientField2());
		Assert.assertNull(result[1].getTransientField1());
		Assert.assertNull(result[2].getTransientField2());
	}

	protected void fillTestXmlObject(TestXmlObject obj, int baseNumber) {
		obj.setTransientField1(new Object());
		obj.setTransientField2(new Object());
		obj.setValueBoolean(true);
		obj.setValueBooleanN(Boolean.valueOf(obj.getValueBoolean()));
		obj.setValueByte((byte) (55 + baseNumber));
		obj.setValueByteN(Byte.valueOf(obj.getValueByte()));
		obj.setValueCharacter((char) (66 + baseNumber));
		obj.setValueCharacterN(Character.valueOf(obj.getValueCharacter()));
		obj.setValueDouble((77 + baseNumber));
		obj.setValueDoubleN(Double.valueOf(obj.getValueDouble()));
		obj.setValueFloat((88 + baseNumber));
		obj.setValueFloatN(Float.valueOf(obj.getValueFloat()));
		obj.setValueInteger((99 + baseNumber));
		obj.setValueIntegerN(Integer.valueOf(obj.getValueInteger()));
		obj.setValueLong((111 + baseNumber));
		obj.setValueLongN(Long.valueOf(obj.getValueLong()));
		obj.setValueString("133_\n" + Integer.toString(baseNumber));
	}

	@SuppressWarnings("unchecked")
	protected <T> T checkCyclic(Object[] targetArray, Class<T> type) {
		String xml = cyclicXmlHandler.write(targetArray);
		Object obj = cyclicXmlHandler.read(xml);

		Assert.assertSame("Wrong class", type, obj.getClass());
		Object[] result = (Object[]) obj;
		Assert.assertEquals("Wrong size", targetArray.length, result.length);
		for (int a = 0, size = targetArray.length; a < size; a++) {
			Assert.assertEquals(targetArray[a], result[a]);
		}
		return (T) result;
	}

	protected void assertObjRefEquals(IObjRef expected, IObjRef actual) {
		Assert.assertNotNull(expected);
		Assert.assertTrue(expected.equals(actual));
		Assert.assertEquals("Wrong RealType", expected.getRealType(), actual.getRealType());
		Assert.assertEquals("Wrong IdNameIndex", expected.getIdNameIndex(), actual.getIdNameIndex());
		Assert.assertEquals("Wrong Id", expected.getId(), actual.getId());
		Assert.assertEquals("Wrong Version", expected.getVersion(), actual.getVersion());
	}
}
