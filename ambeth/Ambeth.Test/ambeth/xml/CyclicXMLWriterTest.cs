using System;
using System.Collections;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Testutil;
using De.Osthus.Ambeth.Xml.Test.Transfer;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace De.Osthus.Ambeth.Xml
{
    [TestClass]
    [TestModule(typeof(BootstrapScannerModule), typeof(XmlModule))]
    public class CyclicXMLWriterTest : AbstractInformationBusTest
    {
        [Autowired(XmlModule.CYCLIC_XML_HANDLER)]
        public ICyclicXmlHandler CyclicXmlHandler { protected get; set; }

        [TestMethod]
        public virtual void WriteSimpleCudResult()
        {
            CUDResult cudResult = new CUDResult();

            String xml = CyclicXmlHandler.Write(cudResult);

            Assert.AssertEquals(XmlTestConstants.XmlOutput[0], xml, "Wrong xml");
        }

        [TestMethod]
        public virtual void WriteEntityMetaData()
        {
            EntityMetaDataTransfer entityMetaData = new EntityMetaDataTransfer();

            String xml = CyclicXmlHandler.Write(entityMetaData);

            Assert.AssertEquals(XmlTestConstants.XmlOutput[1], xml, "Wrong xml");
        }

        [TestMethod]
        public virtual void ReadWriteSimpleCudResult()
        {
            CUDResult cudResult = new CUDResult();

            String xml = CyclicXmlHandler.Write(cudResult);

            Object obj = CyclicXmlHandler.Read(xml);

            Assert.AssertSame(typeof(CUDResult), obj.GetType(), "Wrong class");
            Assert.AssertEquals(XmlTestConstants.XmlOutput[2], xml, "Wrong xml");
        }

        [TestMethod]
        public virtual void WriteClass()
        {
            Object[] array = new Object[3];

            array[0] = typeof(Type);
            array[1] = typeof(IList);
            array[2] = typeof(TestXmlObject);

            String xml = CyclicXmlHandler.Write(array);
            Object obj = CyclicXmlHandler.Read(xml);

            Assert.AssertSame(typeof(Object[]), obj.GetType(), "Wrong class");
            Object[] result = (Object[])obj;
            Assert.AssertEquals(array.Length, result.Length, "Wrong size");
            Assert.AssertSame(array[0], result[0]);
            Assert.AssertSame(array[1], result[1]);
            Assert.AssertSame(array[2], result[2]);
            Assert.AssertEquals(XmlTestConstants.XmlOutput[3], xml, "Wrong xml");
        }

        [TestMethod]
        public virtual void ReadWriteEnum()
        {
            Object[] array = new Object[3];

            array[0] = TestEnum.VALUE_1;
            array[1] = TestEnum.VALUE_2;
            array[2] = TestEnum.VALUE_1;

            String xml = CyclicXmlHandler.Write(array);
            Object obj = CyclicXmlHandler.Read(xml);

            Assert.AssertSame(typeof(Object[]), obj.GetType(), "Wrong class");
            Object[] result = (Object[])obj;
            Assert.AssertEquals(array.Length, result.Length, "Wrong size");
            Assert.AssertEquals(array[0], result[0]);
            Assert.AssertEquals(array[1], result[1]);
            Assert.AssertEquals(array[2], result[2]);
            Assert.AssertEquals(XmlTestConstants.XmlOutput[4], xml, "Wrong xml");
        }

        [TestMethod]
        [Ignore]
        public virtual void ReadWriteSet()
        {
            ISet<Object>[] array = new ISet<Object>[3];

            array[0] = new HashSet<Object>();
            array[1] = new HashSet<Object>();
            array[2] = array[1];

            String xml = CyclicXmlHandler.Write(array);
            Object obj = CyclicXmlHandler.Read(xml);

            Assert.AssertSame(typeof(ISet<Object>[]), obj.GetType(), "Wrong class");
            ISet<Object>[] result = (ISet<Object>[])obj;
            Assert.AssertEquals(array.Length, result.Length, "Wrong size");
            Assert.AssertSame(result[1], result[2]);
            Assert.AssertEquals(array[0].Count, result[0].Count, "Wrong set size at 0");
            Assert.AssertEquals(array[1].Count, result[1].Count, "Wrong set size at 1");
            Assert.AssertEquals(array[2].Count, result[2].Count, "Wrong set size at 2");
            Assert.AssertEquals(XmlTestConstants.XmlOutput[5], xml, "Wrong xml");
        }

        [TestMethod]
        public virtual void ReadWriteList()
        {
            IList<Object>[] array = new IList<Object>[3];

            array[0] = new List<Object>();
            array[1] = new List<Object>();
            array[2] = array[1];

            String xml = CyclicXmlHandler.Write(array);
            Object obj = CyclicXmlHandler.Read(xml);

            Assert.AssertSame(typeof(IList<Object>[]), obj.GetType(), "Wrong class");
            IList<Object>[] result = (IList<Object>[])obj;
            Assert.AssertEquals(array.Length, result.Length, "Wrong size");
            Assert.AssertSame(result[1], result[2]);
            Assert.AssertEquals(array[0].Count, result[0].Count, "Wrong set size at 0");
            Assert.AssertEquals(array[1].Count, result[1].Count, "Wrong set size at 1");
            Assert.AssertEquals(array[2].Count, result[2].Count, "Wrong set size at 2");
            Assert.AssertEquals(XmlTestConstants.XmlOutput[6], xml, "Wrong xml");
        }


        [TestMethod]
        public virtual void ReadWriteObservableCollection()
        {
            ObservableCollection<PrimitiveUpdateItem>[] array = new ObservableCollection<PrimitiveUpdateItem>[3];

            array[0] = new ObservableCollection<PrimitiveUpdateItem>();
            array[0].Add(new PrimitiveUpdateItem());
            array[1] = new ObservableCollection<PrimitiveUpdateItem>();
            array[1].Add(new PrimitiveUpdateItem());
            array[1].Add(new PrimitiveUpdateItem());
            array[2] = array[1];

            String xml = CyclicXmlHandler.Write(array);
            Object obj = CyclicXmlHandler.Read(xml);

            Assert.AssertSame(typeof(IList<PrimitiveUpdateItem>[]), obj.GetType(), "Wrong class");
            IList<PrimitiveUpdateItem>[] result = (IList<PrimitiveUpdateItem>[])obj;
            Assert.AssertEquals(array.Length, result.Length, "Wrong size");
            Assert.AssertSame(result[1], result[2]);
            Assert.AssertEquals(array[0].Count, result[0].Count, "Wrong set size at 0");
            Assert.AssertEquals(array[1].Count, result[1].Count, "Wrong set size at 1");
            Assert.AssertEquals(array[2].Count, result[2].Count, "Wrong set size at 2");
            //Assert.AssertEquals(XmlTestConstants.XmlOutput[6], xml, "Wrong xml");
        }

        [TestMethod]
        public virtual void WriteObjRefs()
        {
            IObjRef[] allOris = new IObjRef[4];

            ObjRef ori = new ObjRef(typeof(String), 2, 4);
            allOris[0] = ori;
            String xml = CyclicXmlHandler.Write(ori);
            Assert.AssertEquals(XmlTestConstants.XmlOutput[15], xml, "Wrong xml");
            Object actual = CyclicXmlHandler.Read(xml);
            Assert.AssertSame(typeof(ObjRef), actual.GetType(), "Wrong class");
            AssertObjRefEquals(ori, (ObjRef)actual);

            ori = new ObjRef(typeof(String), -1, 2, 4);
            allOris[1] = ori;
            xml = CyclicXmlHandler.Write(ori);
            Assert.AssertEquals(XmlTestConstants.XmlOutput[15], xml, "Wrong xml");
            actual = CyclicXmlHandler.Read(xml);
            Assert.AssertSame(typeof(ObjRef), actual.GetType(), "Wrong class");
            AssertObjRefEquals(ori, (ObjRef)actual);

            ori = new ObjRef(typeof(String), 0, "zwei", 4);
            allOris[2] = ori;
            xml = CyclicXmlHandler.Write(ori);
            Assert.AssertEquals(XmlTestConstants.XmlOutput[16], xml, "Wrong xml");
            actual = CyclicXmlHandler.Read(xml);
            Assert.AssertSame(typeof(ObjRef), actual.GetType(), "Wrong class");
            AssertObjRefEquals(ori, (ObjRef)actual);

            ori = new ObjRef(typeof(String), 1, "zwei", 4);
            allOris[3] = ori;
            xml = CyclicXmlHandler.Write(ori);
            Assert.AssertEquals(XmlTestConstants.XmlOutput[17], xml, "Wrong xml");
            actual = CyclicXmlHandler.Read(xml);
            Assert.AssertSame(typeof(ObjRef), actual.GetType(), "Wrong class");
            AssertObjRefEquals(ori, (ObjRef)actual);

            xml = CyclicXmlHandler.Write(allOris);
            actual = CyclicXmlHandler.Read(xml);
            Assert.AssertEquals(allOris.GetType(), actual.GetType());
            IObjRef[] actualArray = (IObjRef[])actual;
            Assert.AssertEquals(allOris.Length, actualArray.Length);
            for (int i = 0; i < allOris.Length; i++)
            {
                AssertObjRefEquals((ObjRef)allOris[i], (ObjRef)actualArray[i]);
            }
        }

        [TestMethod]
        public virtual void NativeInteger()
        {
            int[] array = new int[3];

            array[0] = Int32.MaxValue;
            array[1] = Int32.MinValue;
            array[2] = 3;

            String xml = CyclicXmlHandler.Write(array);
            Object obj = CyclicXmlHandler.Read(xml);

            Assert.AssertSame(typeof(int[]), obj.GetType(), "Wrong class");
            int[] result = (int[])obj;
            Assert.AssertEquals(array.Length, result.Length, "Wrong size");
            Assert.AssertEquals(array[0], result[0]);
            Assert.AssertEquals(array[1], result[1]);
            Assert.AssertEquals(array[2], result[2]);
            Assert.AssertEquals(XmlTestConstants.XmlOutput[7], xml, "Wrong xml");
        }

        [TestMethod]
        public virtual void NativeLong()
        {
            long[] array = new long[3];

            array[0] = Int64.MaxValue;
            array[1] = Int64.MinValue;
            array[2] = 3;

            String xml = CyclicXmlHandler.Write(array);
            Object obj = CyclicXmlHandler.Read(xml);

            Assert.AssertSame(typeof(long[]), obj.GetType(), "Wrong class");
            long[] result = (long[])obj;
            Assert.AssertEquals(array.Length, result.Length, "Wrong size");
            Assert.AssertEquals(array[0], result[0]);
            Assert.AssertEquals(array[1], result[1]);
            Assert.AssertEquals(array[2], result[2]);
            Assert.AssertEquals(XmlTestConstants.XmlOutput[8], xml, "Wrong xml");
        }

        [TestMethod]
        [Ignore]
        public virtual void NativeDouble()
        {
            double[] array = new double[3];

            array[0] = Double.MaxValue;
            array[1] = Double.MinValue;
            array[2] = 3;

            String xml = CyclicXmlHandler.Write(array);
            Object obj = CyclicXmlHandler.Read(xml);

            Assert.AssertSame(typeof(double[]), obj.GetType(), "Wrong class");
            double[] result = (double[])obj;
            Assert.AssertEquals(array.Length, result.Length, "Wrong size");
            Assert.AssertEquals(array[0], result[0]);
            Assert.AssertEquals(array[1], result[1]);
            Assert.AssertEquals(array[2], result[2]);
            Assert.AssertEquals(XmlTestConstants.XmlOutput[9], xml, "Wrong xml");
        }

        [TestMethod]
        [Ignore]
        public virtual void NativeFloat()
        {
            float[] array = new float[3];

            array[0] = Single.MaxValue;
            array[1] = Single.MinValue;
            array[2] = 3;

            String xml = CyclicXmlHandler.Write(array);
            Object obj = CyclicXmlHandler.Read(xml);

            Assert.AssertSame(typeof(float[]), obj.GetType(), "Wrong class");
            float[] result = (float[])obj;
            Assert.AssertEquals(array.Length, result.Length, "Wrong size");
            array[0].Equals(result[0]);
            Assert.AssertEquals(array[0], result[0]);
            Assert.AssertEquals(array[1], result[1]);
            Assert.AssertEquals(array[2], result[2]);
            Assert.AssertEquals(XmlTestConstants.XmlOutput[10], xml, "Wrong xml");
        }

        [TestMethod]
        public virtual void NativeShort()
        {
            short[] array = new short[3];

            array[0] = Int16.MaxValue;
            array[1] = Int16.MinValue;
            array[2] = 3;

            String xml = CyclicXmlHandler.Write(array);
            Object obj = CyclicXmlHandler.Read(xml);

            Assert.AssertSame(typeof(short[]), obj.GetType(), "Wrong class");
            short[] result = (short[])obj;
            Assert.AssertEquals(array.Length, result.Length, "Wrong size");
            Assert.AssertEquals(array[0], result[0]);
            Assert.AssertEquals(array[1], result[1]);
            Assert.AssertEquals(array[2], result[2]);
            Assert.AssertEquals(XmlTestConstants.XmlOutput[11], xml, "Wrong xml");
        }

        [TestMethod]
        public virtual void NativeByte()
        {
            sbyte[] array = new sbyte[3];

            array[0] = SByte.MaxValue;
            array[1] = SByte.MinValue;
            array[2] = 3;

            String xml = CyclicXmlHandler.Write(array);
            Object obj = CyclicXmlHandler.Read(xml);

            Assert.AssertSame(typeof(sbyte[]), obj.GetType(), "Wrong class");
            sbyte[] result = (sbyte[])obj;
            Assert.AssertEquals(array.Length, result.Length, "Wrong size");
            Assert.AssertEquals(array[0], result[0]);
            Assert.AssertEquals(array[1], result[1]);
            Assert.AssertEquals(array[2], result[2]);
            Assert.AssertEquals(XmlTestConstants.XmlOutput[12], xml, "Wrong xml");
        }

        [TestMethod]
        public virtual void NativeUnsignedByte()
        {
            byte[] array = new byte[3];

            array[0] = Byte.MaxValue;
            array[1] = Byte.MinValue;
            array[2] = 3;

            String xml = CyclicXmlHandler.Write(array);
            Object obj = CyclicXmlHandler.Read(xml);

            Assert.AssertSame(typeof(byte[]), obj.GetType(), "Wrong class");
            byte[] result = (byte[])obj;
            Assert.AssertEquals(array.Length, result.Length, "Wrong size");
            Assert.AssertEquals(array[0], result[0]);
            Assert.AssertEquals(array[1], result[1]);
            Assert.AssertEquals(array[2], result[2]);
            Assert.AssertEquals("<root><a i=\"1\" s=\"3\" ti=\"2\" n=\"UByte\"><values v=\"/wAD\"/></a></root>", xml, "Wrong xml");
        }

        [TestMethod]
        public virtual void NativeCharacter()
        {
            char[] array = new char[3];

            array[0] = Char.MaxValue;
            array[1] = Char.MinValue;
            array[2] = (char)3;

            String xml = CyclicXmlHandler.Write(array);
            Object obj = CyclicXmlHandler.Read(xml);

            Assert.AssertSame(typeof(char[]), obj.GetType(), "Wrong class");
            char[] result = (char[])obj;
            Assert.AssertEquals(array.Length, result.Length, "Wrong size");
            Assert.AssertEquals(array[0], result[0]);
            Assert.AssertEquals(array[1], result[1]);
            Assert.AssertEquals(array[2], result[2]);
            Assert.AssertEquals(XmlTestConstants.XmlOutput[13], xml, "Wrong xml");
        }

        [TestMethod]
        public virtual void NativeBoolean()
        {
            bool[] array = new bool[3];
            array[0] = true;
            array[1] = false;
            array[2] = true;

            String xml = CyclicXmlHandler.Write(array);
            Object obj = CyclicXmlHandler.Read(xml);

            Assert.AssertSame(typeof(bool[]), obj.GetType(), "Wrong class");
            bool[] result = (bool[])obj;
            Assert.AssertEquals(array.Length, result.Length, "Wrong size");
            Assert.AssertEquals(array[0], result[0]);
            Assert.AssertEquals(array[1], result[1]);
            Assert.AssertEquals(array[2], result[2]);
            Assert.AssertEquals(XmlTestConstants.XmlOutput[14], xml, "Wrong xml");
        }

        [TestMethod]
        public virtual void CyclicString()
        {
            String[] array = new String[4];
            array[0] = "HalloEinfach]]";
            array[1] = "Hall\n\to]]>MeinTest]]>AB]]AB]>AB";
            array[2] = array[0];
            array[3] = "hallo2";

            String[] result = (String[])CheckCyclic<String[]>(array);
            Assert.AssertSame(result[2], result[0]);
        }

        [TestMethod]
        public virtual void CyclicString2()
        {
            String xml = "<root><a i=\"1\" s=\"2\" ti=\"2\" n=\"Object\"><o i=\"3\" ti=\"4\" n=\"CUDResult\" m=\"AllChanges\"><l i=\"5\" s=\"2\" ti=\"6\" n=\"IChangeContainer\"><o i=\"7\" ti=\"8\" n=\"CreateContainer\" m=\"Primitives Reference Relations\"><a i=\"9\" s=\"4\" ti=\"10\" n=\"IPUI\"><o i=\"11\" ti=\"12\" n=\"PrimitiveUpdateItem\" m=\"NewValue MemberName\"><e i=\"13\" ti=\"14\" n=\"OrderStateType\" ns=\"Comtrack\" v=\"OPEN\"/><s i=\"15\"><![CDATA[State]]></s></o><o i=\"16\" ti=\"12\" m=\"NewValue MemberName\"><e i=\"17\" ti=\"18\" n=\"OrderType\" ns=\"Comtrack\" v=\"FTE\"/><s i=\"19\"><![CDATA[OrderType]]></s></o><o i=\"20\" ti=\"12\" m=\"NewValue MemberName\"><s i=\"21\"/><s i=\"22\"><![CDATA[Workgroup]]></s></o><o i=\"23\" ti=\"12\" m=\"NewValue MemberName\"><s i=\"24\"><![CDATA[test4]]></s><s i=\"25\"><![CDATA[Supplier]]></s></o></a><o i=\"26\" ti=\"27\" n=\"DirectObjRef\" m=\"RealType IdNameIndex CreateContainerIndex\"><c i=\"28\" n=\"Ordr\" ns=\"Comtrack\"/><o i=\"29\" ti=\"30\" n=\"ByteN\" v=\"-1\"/><o i=\"31\" ti=\"32\" n=\"Int32N\" v=\"1\"/></o><a i=\"33\" s=\"1\" ti=\"34\" n=\"IRUI\"><o i=\"35\" ti=\"36\" n=\"RelationUpdateItem\" m=\"AddedORIs MemberName\"><a i=\"37\" s=\"1\" ti=\"38\" n=\"IObjRef\"><o i=\"39\" ti=\"27\"><c i=\"40\" n=\"Compound\" ns=\"Comtrack\"/><r i=\"29\"/><r i=\"31\"/></o></a><s i=\"41\"><![CDATA[Compounds]]></s></o></a></o><o i=\"42\" ti=\"8\" m=\"Primitives Reference Relations\"><a i=\"43\" s=\"3\" ti=\"10\"><o i=\"44\" ti=\"12\" m=\"NewValue MemberName\"><s i=\"45\"><![CDATA[test4]]></s><s i=\"46\"><![CDATA[Description]]></s></o><o i=\"47\" ti=\"12\" m=\"NewValue MemberName\"><e i=\"48\" ti=\"49\" n=\"CompoundStateType\" ns=\"Comtrack\" v=\"INITIALIZED\"/><s i=\"50\"><![CDATA[State]]></s></o><o i=\"51\" ti=\"12\" m=\"NewValue MemberName\"><o ti=\"52\" n=\"Int64\" v=\"1\"/><s i=\"53\"><![CDATA[DetailNumber]]></s></o></a><r i=\"39\"/><a i=\"54\" s=\"1\" ti=\"34\"><o i=\"55\" ti=\"36\" m=\"AddedORIs MemberName\"><a i=\"56\" s=\"1\" ti=\"38\"><r i=\"26\"/></a><s i=\"57\"><![CDATA[Order]]></s></o></a></o></l></o><o i=\"58\" ti=\"59\" n=\"MethodDescription\" m=\"ServiceType ParamTypes MethodName\"><c i=\"60\" n=\"IOrderService\"/><a i=\"61\" s=\"2\" ti=\"62\" n=\"Class\"><c i=\"63\" n=\"Ordr[]\" ns=\"Comtrack\"/><r i=\"63\"/></a><s i=\"64\"><![CDATA[Save]]></s></o></a></root>";
            Object obj = CyclicXmlHandler.Read(xml);
        }

        [TestMethod]
        public virtual void CyclicBoolean()
        {
            Boolean?[] array = new Boolean?[3];
            array[0] = true;
            array[1] = false;
            array[2] = true;

            Boolean?[] result = (Boolean?[])CheckCyclic<Boolean?[]>(array);
        }

        [TestMethod]
        public virtual void CyclicByte()
        {
            Byte?[] array = new Byte?[3];
            array[0] = (byte)10;
            array[1] = (byte)20;
            array[2] = array[0];

            Byte?[] result = (Byte?[])CheckCyclic<Byte?[]>(array);
        }

        [TestMethod]
        public virtual void CyclicCharacter()
        {
            Char?[] array = new Char?[3];
            array[0] = 'A';
            array[1] = '&';
            array[2] = array[0];

            Char?[] result = CheckCyclic<Char?[]>(array);
        }

        [TestMethod]
        public virtual void CyclicShort()
        {
            Int16?[] array = new Int16?[3];
            array[0] = Int16.MaxValue;
            array[1] = Int16.MinValue;
            array[2] = array[0];

            Int16?[] result = CheckCyclic<Int16?[]>(array);
        }

        [TestMethod]
        [Ignore]
        public virtual void CyclicFloat()
        {
            Single?[] array = new Single?[3];
            array[0] = Single.MaxValue;
            array[1] = Single.MinValue;
            array[2] = array[0];

            Single?[] result = CheckCyclic<Single?[]>(array);
        }

        [TestMethod]
        public virtual void CyclicInteger()
        {
            Int32?[] array = new Int32?[3];
            array[0] = Int32.MaxValue;
            array[1] = Int32.MinValue;
            array[2] = array[0];

            Int32?[] result = CheckCyclic<Int32?[]>(array);
        }

        [TestMethod]
        [Ignore]
        public virtual void CyclicDouble()
        {
            Double?[] array = new Double?[3];
            array[0] = Double.MaxValue;
            array[1] = Double.MinValue;
            array[2] = array[0];
            array[3] = new Nullable<Double>();
            array[4] = 17;

            Double?[] result = CheckCyclic<Double?[]>(array);
        }

        [TestMethod]
        public virtual void CyclicLong()
        {
            Int64?[] array = new Int64?[3];
            array[0] = Int64.MaxValue;
            array[1] = Int64.MinValue;
            array[2] = array[0];

            Int64?[] result = CheckCyclic<Int64?[]>(array);
        }

        [TestMethod]
        public virtual void CyclicDate()
        {
            DateTime[] array = new DateTime[3];
            array[0] = new DateTime(1981, 5, 3, 17, 18, 19, DateTimeKind.Utc);
            array[1] = new DateTime(1987, 11, 22, 14, 12, 45, DateTimeKind.Utc);
            array[2] = array[0];

            DateTime[] result = CheckCyclic<DateTime[]>(array);
        }

        [TestMethod]
        public virtual void CyclicTestXmlObject()
        {
            TestXmlObject[] array = new TestXmlObject[3];
            array[0] = new TestXmlObject();
            array[1] = new TestXmlObject();
            array[2] = array[0];
            FillTestXmlObject(array[0], 23);
            FillTestXmlObject(array[1], 46);

            TestXmlObject[] result = CheckCyclic<TestXmlObject[]>(array);
            Assert.AssertSame(result[2], result[0]);
            Assert.AssertNull(result[0].TransientField1);
            Assert.AssertNull(result[0].TransientField2);
            Assert.AssertNull(result[1].TransientField1);
            Assert.AssertNull(result[2].TransientField2);
        }

        protected void FillTestXmlObject(TestXmlObject obj, int baseNumber)
        {
            obj.TransientField1 = new Object();
            obj.TransientField2 = new Object();
            obj.ValueBoolean = true;
            obj.ValueBooleanN = obj.ValueBoolean;
            obj.ValueByte = (byte)(55 + baseNumber);
            obj.ValueByteN = obj.ValueByte;
            obj.ValueCharacter = (char)(66 + baseNumber);
            obj.ValueCharacterN = obj.ValueCharacter;
            obj.ValueDouble = (77 + baseNumber);
            obj.ValueDoubleN = obj.ValueDouble;
            obj.ValueFloat = (88 + baseNumber);
            obj.ValueFloatN = obj.ValueFloat;
            obj.ValueInteger = (99 + baseNumber);
            obj.ValueIntegerN = obj.ValueInteger;
            obj.ValueLong = (111 + baseNumber);
            obj.ValueLongN = obj.ValueLong;
            obj.ValueString = "133_" + baseNumber;
        }

        protected T CheckCyclic<T>(Array targetArray)
        {
            String xml = CyclicXmlHandler.Write(targetArray);
            Object obj = CyclicXmlHandler.Read(xml);

            Assert.AssertSame(typeof(T), obj.GetType(), "Wrong class");
            Array result = (Array)obj;
            Assert.AssertEquals(targetArray.Length, result.Length, "Wrong size");
            Assert.AssertEquals(targetArray.GetValue(0), result.GetValue(0));
            Assert.AssertEquals(targetArray.GetValue(1), result.GetValue(1));
            Assert.AssertEquals(targetArray.GetValue(2), result.GetValue(2));
            return (T)obj;
        }

        protected void AssertObjRefEquals(ObjRef expected, ObjRef actual)
        {
            Assert.AssertNotNull(expected);
            Assert.AssertTrue(expected.Equals(actual));
            Assert.AssertEquals(expected.RealType, actual.RealType, "Wrong RealType");
            Assert.AssertEquals(expected.IdNameIndex, actual.IdNameIndex, "Wrong IdNameIndex");
            Assert.AssertEquals(expected.Id, actual.Id, "Wrong Id");
            Assert.AssertEquals(expected.Version, actual.Version, "Wrong Version");
        }
    }
}
