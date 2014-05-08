using System;
using System.Collections.Generic;
using System.Linq;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace De.Osthus.Ambeth.Util.Test
{
    [TestClass]
    public class ListUtilTest
    {
        protected static IList<Object> expectedList;

        protected static ISet<Object> expectedSet;

        protected static IList<Object> emptyList;

	    [ClassInitialize]
        public static void ClassInit(TestContext context)
	    {
		    expectedList = new List<Object>();
		    expectedList.Add(2);
            String duplicatEntry = "Duplicat Entry";
            expectedList.Add(duplicatEntry);
		    expectedList.Add("test");
            expectedList.Add(duplicatEntry);

		    expectedSet = new HashSet<Object>(expectedList);

            emptyList = new List<Object>();
	    }

        [TestMethod]
        public void TestSetUp()
        {
            Assert.IsTrue(expectedList.Count > expectedSet.Count);
        }

        [TestMethod]
        public void TestToArray()
        {
            Object[] actual = ListUtil.ToArray<Object>(expectedList);
            AssertSimilar(expectedList, actual.ToList<Object>());
        }

	    [TestMethod]
	    [Ignore]
	    public void TestToListEnumerationOfT()
	    {
            Assert.Fail("Not yet implemented"); // TODO
	    }

	    [TestMethod]
	    [Ignore]
	    public void TestToListIterableOfT()
	    {
            Assert.Fail("Not yet implemented"); // TODO
        }

	    [TestMethod]
	    [Ignore]
	    public void TestToListIObjectCollectorIterableOfT()
	    {
            Assert.Fail("Not yet implemented"); // TODO
        }

	    [TestMethod]
	    [Ignore]
	    public void TestCreateCollectionOfType()
	    {
            Assert.Fail("Not yet implemented"); // TODO
        }

	    [TestMethod]
	    public void testAnyToList_List()
	    {
		    IList<Object> actual = ListUtil.AnyToList(expectedList);
		    AssertSimilar(expectedList, actual);
		    Assert.AreSame(expectedList, actual);
	    }

	    [TestMethod]
	    public void testAnyToList_Set()
	    {
            IList<Object> expected = new List<Object>(expectedSet);
            IList<Object> actual = ListUtil.AnyToList(expectedSet);
		    AssertSimilar(expected, actual);
	    }

	    [TestMethod]
        public void testAnyToList_Collection()
	    {
            IDictionary<Int32, String> input = new Dictionary<Int32, String>();
            input.Add(4, "four");
            input.Add(7, "seven");
            IList<Object> actual = ListUtil.AnyToList(input);
            Assert.IsNotNull(actual);
            Assert.AreEqual(input.Count, actual.Count);
            IEnumerator<KeyValuePair<Int32, String>> enumerator = input.GetEnumerator();
            while (enumerator.MoveNext())
            {
                Assert.IsTrue(actual.Contains(enumerator.Current));
            }
        }

	    [TestMethod]
        public void testAnyToList_Array1()
        {
            Object[] input = new Object[expectedList.Count];
            expectedList.CopyTo(input, 0);
            IList<Object> actual = ListUtil.AnyToList(input);
	        AssertSimilar(expectedList, actual);
        }

        [TestMethod]
        public void testAnyToList_Array2()
        {
	        Int32[] input = { 1, 2, 3, 4, 5 };
            IList<Object> expected = new List<Object>(input.Length);
            for (uint i = 0; i < input.Length; i++)
            {
                expected.Add(input[i]);
            }
            IList<Object> actual = ListUtil.AnyToList(input);
	        AssertSimilar(expected, actual);
        }

        [TestMethod]
        public void testAnyToList_Array3()
        {
            Int32[] input = { };
            IList<Object> actual = ListUtil.AnyToList(input);
            AssertSimilar(emptyList, actual);
        }

	    [TestMethod]
	    public void testAnyToList_Object()
	    {
		    Int32 testItem = 9843789;
            IList<Object> actual = ListUtil.AnyToList(testItem);
		    Assert.IsNotNull(actual);
		    Assert.AreEqual(1, actual.Count);
		    Assert.AreEqual(testItem, actual[0]);
	    }

        [TestMethod]
        public void testAnyToSet_List()
        {
            ISet<Object> actual = ListUtil.AnyToSet(expectedList);
            AssertSimilar(expectedSet, actual);
        }

        [TestMethod]
        public void testAnyToSet_Set()
        {
            ISet<Object> actual = ListUtil.AnyToSet(expectedSet);
            AssertSimilar(expectedSet, actual);
        }

        [TestMethod]
        public void testAnyToSet_Collection()
        {
            IDictionary<Int32, String> input = new Dictionary<Int32, String>();
            input.Add(4, "four");
            input.Add(7, "seven");
            ISet<Object> actual = ListUtil.AnyToSet(input);
            Assert.IsNotNull(actual);
            Assert.AreEqual(input.Count, actual.Count);
            IEnumerator<KeyValuePair<Int32, String>> enumerator = input.GetEnumerator();
            while (enumerator.MoveNext())
            {
                Assert.IsTrue(actual.Contains(enumerator.Current));
            }
        }

        [TestMethod]
        public void testAnyToSet_Array1()
        {
            Object[] input = new Object[expectedList.Count];
            expectedList.CopyTo(input, 0);
            ISet<Object> actual = ListUtil.AnyToSet(input);
            AssertSimilar(expectedList, actual);
        }

        [TestMethod]
        public void testAnyToSet_Array2()
        {
            Int32[] input = { 1, 2, 3, 4, 5 };
            ISet<Object> expected = new HashSet<Object>();
            for (uint i = 0; i < input.Length; i++)
            {
                expected.Add(input[i]);
            }
            ISet<Object> actual = ListUtil.AnyToSet(input);
            AssertSimilar(expected, actual);
        }

        [TestMethod]
        public void testAnyToSet_Object()
        {
            Int32 testItem = 9843789;
            ISet<Object> actual = ListUtil.AnyToSet(testItem);
            Assert.IsNotNull(actual);
            Assert.AreEqual(1, actual.Count);
            IEnumerator<Object> enumerator = actual.GetEnumerator();
            Assert.IsTrue(enumerator.MoveNext());
            Assert.AreEqual(testItem, enumerator.Current);
        }

	    protected void AssertSimilar(IList<Object> expected, IList<Object> actual)
	    {
		    Assert.IsNotNull(expected);
            Assert.IsNotNull(actual);
            Assert.AreEqual(expected.Count, actual.Count);
            for (int i = actual.Count; i-- > 0; )
		    {
                Assert.AreEqual(expected[i], actual[i]);
		    }
	    }

	    protected void AssertSimilar(IList<Object> expected, ISet<Object> actual)
	    {
            Assert.IsNotNull(expected);
            Assert.IsNotNull(actual);
            for (int i = actual.Count; i-- > 0; )
		    {
                Assert.IsTrue(actual.Contains(expected[i]));
		    }
	    }

	    protected void AssertSimilar(ISet<Object> expected, ISet<Object> actual)
	    {
            Assert.IsNotNull(expected);
            Assert.IsNotNull(actual);
            Assert.AreEqual(expected.Count, actual.Count);
            IEnumerator<Object> iter = expected.GetEnumerator();
		    while (iter.MoveNext())
		    {
			    Assert.IsTrue(actual.Contains(iter.Current));
		    }
	    }
    }
}
